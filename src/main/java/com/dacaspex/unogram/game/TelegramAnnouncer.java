package com.dacaspex.unogram.game;

import com.dacaspex.unogram.main.TelegramFormatter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramAnnouncer implements Announcer {

    private final TelegramLongPollingBot bot;
    private final TelegramFormatter formatter;
    private Chat groupChat;
    private Map<Player, User> userMap;
    private Map<Player, Chat> chatMap;

    public TelegramAnnouncer(TelegramLongPollingBot bot) {
        this.bot = bot;
        this.groupChat = null;
        this.userMap = new HashMap<>();
        this.chatMap = new HashMap<>();
        this.formatter = new TelegramFormatter();
    }

    public void setGroupChat(Chat groupChat) {
        this.groupChat = groupChat;
    }

    public void addUserMapping(User user, Player player, Chat chat) {
        this.userMap.put(player, user);
        this.chatMap.put(player, chat);
    }

    @Override
    public void playerJoinedParty(Player player) {
        sendPersonalMessage(
                player,
                "You successfully joined this game of Uno! Your cards and actions will appear in this " +
                        "space. Watch the group chat for the state of the game."
        );

        User user = userMap.get(player);
        sendGroupMessage(String.format("@%s joined the game!", user.getUserName()));
    }

    @Override
    public void playerLeftParty(Player player) {
        sendPersonalMessage(player,"You successfully left the game.");
        sendGroupMessage(String.format("%s left the game.", player.getUsername()));
    }

    @Override
    public void playerAlreadyInParty(Player player) {
        // TODO: Add game id
        // TODO: Rename method?
        sendPersonalMessage(player, "You already joined the game.");
    }

    @Override
    public void playerNotInParty(Player player) {
        // TODO: Rename method?
        sendPersonalMessage(player, "You are not yet in the party.");
    }

    @Override
    public void gameStarted(UnoGame game) {
        // TODO: Show players and player order in the group (pinned message?)

        Card topCard = game.getDiscardPile().getCards().peek();
        Player startingPlayer = game.getParty().getCurrent();
        List<Player> orderedPlayers = game.getParty().getOrderedPlayers();

        sendGroupMessage(
                String.format(
                        "The game has started! The player order is %s. The top of the " +
                                "discard pile shows %s. @%s, it is your turn to play a card.",
                        formatter.formatPlayers(orderedPlayers),
                        formatter.formatCard(topCard),
                        startingPlayer.getUsername()
                )
        );

        orderedPlayers.forEach(player -> sendPersonalMessage(
                player,
                String.format(
                        "The game has started. Here are your cards\n%s",
                        formatter.formatHand(player.getHand())
                )
        ));

        sendPersonalMessage(
                startingPlayer,
                String.format(
                        "It is your turn. The top of the discard pile shows: %s",
                        formatter.formatCard(topCard)
                )
        );
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        // TODO: Explain why
        sendPersonalMessage(player, "That card cannot be played.");
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        // TODO: Explain who's turn it is
        sendPersonalMessage(player, "It is not your turn.");
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        // TODO: Don't make these private announcements, but rather public
        // TODO: Pinned message with player order?

        // TODO: Fix next player
        String effectText = "";
        try {
            switch (card.getType()) {
                case SKIP:
                    Player skippedPlayer = game.getParty().getNext();
                    effectText = String.format("%s was skipped.", skippedPlayer.getUsername());
                    break;
                case REVERSE:
                    Player reversedPlayer = game.getParty().getPrevious();
                    effectText = "The order is now reversed.";
                    break;
                case DRAW_2:
                    Player nextPlayer = game.getParty().getNext();
                    effectText = String.format("%s drew 2 cards.", nextPlayer.getUsername());
                    break;
            }

            // TODO: From this point...

            // Notify group about card that has been played
            Player nextPlayer = game.getParty().getNext();
            SendMessage groupMessage = new SendMessage()
                    .setChatId(groupChat.getId())
                    .setText(
                            String.format(
                                    "@%s played a %s card. Next player is %s",
                                    player.getUsername(),
                                    card,
                                    nextPlayer.getUsername()
                            )
                    );
            bot.execute(groupMessage);

            // Notify next player that it is his/her turn
            Hand nextPlayerHand = nextPlayer.getHand();
            SendMessage notifyNextPlayerMessage = new SendMessage()
                    .setChatId(chatMap.get(nextPlayer).getId())
                    .setText("It is your turn to play a card. This is your hand:\n" + formatter.formatHand(nextPlayerHand));
            bot.execute(notifyNextPlayerMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {

    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        sendPersonalMessage(player, String.format("You drew a %s card", card));
        sendGroupMessage(String.format("%s drew a card.", player.getUsername()));
    }

    private void sendPersonalMessage(Player player, String message) {
        long chatId = chatMap.get(player).getId();

        try {
            bot.execute(new SendMessage(chatId, message));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }

    private void sendGroupMessage(String message) {
        try {
            bot.execute(new SendMessage(groupChat.getId(), message));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }
}
