package com.dacaspex.unogram.game;

import com.dacaspex.unogram.main.MessageFormatter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class TelegramAnnouncer implements Announcer {

    private final TelegramLongPollingBot bot;
    private final MessageFormatter formatter;
    private Chat groupChat;
    private Map<Player, User> userMap;
    private Map<Player, Chat> chatMap;

    public TelegramAnnouncer(TelegramLongPollingBot bot) {
        this.bot = bot;
        this.groupChat = null;
        this.userMap = new HashMap<>();
        this.chatMap = new HashMap<>();
        this.formatter = new MessageFormatter();
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
        long playerChatId = chatMap.get(player).getId();
        User user = userMap.get(player);

        SendMessage personalMessage = new SendMessage()
                .setChatId(playerChatId)
                .setText("You successfully joined this game of Uno! Your cards and actions will appear in this space. " +
                        "Watch the group chat for the state of the game.");
        SendMessage groupMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText(String.format("@%s joined the game", user.getUserName()));

        try {
            bot.execute(personalMessage);
            bot.execute(groupMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }

    @Override
    public void playerLeftParty(Player player) {

    }

    @Override
    public void playerAlreadyInParty(Player player) {

    }

    @Override
    public void playerNotInParty(Player player) {

    }

    @Override
    public void gameStarted(UnoGame game) {
        // TODO: Try/catch

        // TODO: Notify group that game has started
        // TODO: Shows cards to each player
        // TODO: Show top of discard pile in the group
        // TODO: Notify starting player privately and in the group
        // TODO: Show players and player order in the group (pinned message?)

        // Notify players about their hand
        for (Player player : game.getParty().getPlayers()) {
            Chat chat = chatMap.get(player);
            String message = String.format(
                    "@%s, here are your cards\n%s",
                    player.getUsername(),
                    formatter.formatHand(player.getHand())
            );

            SendMessage sendMessage = new SendMessage()
                    .setChatId(chat.getId())
                    .setText(message);
            try {
                bot.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        Player startingPlayer = game.getParty().getCurrent();
        Deck discardPile = game.getDiscardPile();
        SendMessage startingGameMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText("Starting the game, the first player to play is: " + startingPlayer.getUsername());
        SendMessage cardMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText("Top of discard pile shows: " + discardPile.getCards().peek());

        try {
            bot.execute(startingGameMessage);
            bot.execute(cardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        Chat chat = chatMap.get(player);
        SendMessage invalidCardMessage = new SendMessage()
                .setChatId(chat.getId())
                .setText("That card cannot be played");
        try {
            bot.execute(invalidCardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        Chat chat = chatMap.get(player);
        SendMessage noTurnMessage = new SendMessage()
                .setChatId(chat.getId())
                .setText("It is not your turn.");
        try {
            bot.execute(noTurnMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        // TODO: Don't make these private announcements, but rather public
        // TODO: Pinned message with player order?

        // TODO: Make specific methods for each card, provide information about next / previous players
        try {
            switch (card.getType()) {
                case SKIP:
                    Player skippedPlayer = game.getParty().getPrevious();
                    SendMessage skipMessage = new SendMessage()
                            .setChatId(chatMap.get(skippedPlayer).getId())
                            .setText("You were skipped because of a skip card");
                    bot.execute(skipMessage);
                    break;
                case REVERSE:
                    Player reversedPlayer = game.getParty().getPrevious(2);
                    SendMessage reverseMessage = new SendMessage()
                            .setChatId(chatMap.get(reversedPlayer).getId())
                            .setText("You were skipped because of a reverse card");
                    bot.execute(reverseMessage);
                    break;
                case DRAW_2:
                    Player nextPlayer = game.getParty().getCurrent();
                    SendMessage drawMessage = new SendMessage()
                            .setChatId(chatMap.get(nextPlayer).getId())
                            .setText("You were forced to draw 2 cards");
                    bot.execute(drawMessage);
                    break;
            }

            // Notify group about card that has been played
            Player nextPlayer = game.getParty().getCurrent();
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
        // Notify player
        SendMessage playerMessage = new SendMessage()
                .setChatId(chatMap.get(player).getId())
                .setText(String.format("You drew a %s card", card));

        // Notify group
        SendMessage groupMessage = new SendMessage()
                .setChatId(groupChat.getId())
                .setText(String.format("%s drew a card", player.getUsername()));

        try {
            bot.execute(playerMessage);
            bot.execute(groupMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
