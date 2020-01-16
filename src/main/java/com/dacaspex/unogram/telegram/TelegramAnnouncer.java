package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.game.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
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
    private Map<Player, User> userMap;
    private Map<Player, Chat> chatMap;

    public TelegramAnnouncer(TelegramLongPollingBot bot) {
        this.bot = bot;
        this.userMap = new HashMap<>();
        this.chatMap = new HashMap<>();
        this.formatter = new TelegramFormatter();
    }

    public void addUserMapping(User user, Player player, Chat chat) {
        userMap.put(player, user);
        chatMap.put(player, chat);
    }

    public void removeUserMapping(Player player) {
        userMap.remove(player);
        chatMap.remove(player);
    }

    @Override
    public void gameCreated(String id, Player host) {
        sendPersonalMessage(
                host,
                String.format(
                        "You successfully created this game with id %s! You can forward the following message " +
                                "to your friends such that they can join.",
                        id
                )
        );
        sendPersonalMessage(
                host,
                String.format(
                        "Do you want to play a game of Uno with me? Send a message to @%s with /join %s.",
                        bot.getBotUsername(),
                        id
                )
        );
    }

    @Override
    public void playerJoinedParty(Player player, Party party) {
        // TODO: Update chat title with game id?
        sendPersonalMessage(
                player,
                "You successfully joined this game of Uno! Your cards and actions will appear in this space. " +
                        "Waiting for the game to start."
        );

        party.getPlayers().stream().filter(p -> p != player).forEach(p -> {
            sendPersonalMessage(
                    p,
                    String.format("@%s has joined the game.", player.getUsername())
            );
        });
    }

    @Override
    public void playerLeftParty(Player player, Party party) {
        sendPersonalMessage(player, "You successfully left the game.");
    }

    @Override
    public void playerAlreadyInParty(Player player, Party party) {
        // TODO: Add game id
        // TODO: Rename method?
        sendPersonalMessage(player, "You already joined the game.");
    }

    @Override
    public void playerNotInParty(Player player, Party party) {
        // TODO: Rename method?
        sendPersonalMessage(player, "You are not yet in the party.");
    }

    @Override
    public void gameStarted(UnoGame game) {
        Card topCard = game.getDiscardPile().getCards().peek();
        Player startingPlayer = game.getParty().getCurrent();
        List<Player> orderedPlayers = game.getParty().getOrderedPlayers();

        orderedPlayers.forEach(player -> {
            sendPersonalMessage(
                    player,
                    String.format(
                            "The game has started! The player order is %s. The top of the discard pile shows %s." +
                                    " Here are your cards:\n%s",
                            formatter.formatPlayers(orderedPlayers),
                            formatter.formatCard(topCard),
                            formatter.formatHand(player.getHand())
                    ),
                    ParseMode.MARKDOWN
            );
            sendPersonalMessage(
                    player,
                    "When it is your turn, you can play a card by typing /play <index>, play a wild card " +
                            "with /play <index> <suit> or draw a card by /draw. For example: /play 5 or " +
                            "/play 3 red (to play a wild card and change the suit to red). Good luck!"
            );
        });

        sendPersonalMessage(startingPlayer, "You are the first to play a card! Choose from a card above.");
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
        if (game.isFinished()) {
            game.getParty().getPlayers().forEach(p -> sendPersonalMessage(
                    p,
                    String.format(
                            "@%s played a %s and that was his last card.",
                            p.getUsername(),
                            formatter.formatCard(card)
                    )
            ));

            return;
        }

        String effectText = "";
        switch (card.getType()) {
            case SKIP:
                Player skippedPlayer = game.getParty().getNext();
                effectText = String.format("%s was skipped", skippedPlayer.getUsername());
                break;
            case REVERSE:
                effectText = "The order is now reversed";
                break;
            case DRAW_2:
                Player nextPlayer = game.getParty().getNext();
                effectText = String.format("%s drew 2 cards", nextPlayer.getUsername());
                break;
        }

        Player nextPlayer = game.getParty().getNext();
        for (Player p : game.getParty().getPlayers()) {
            sendPersonalMessage(
                    p,
                    String.format(
                            "- @%s played a %s card. %s.\n" +
                                    "- Next player is @%s.\n",
                            player.getUsername(),
                            formatter.formatCard(card),
                            effectText,
                            nextPlayer.getUsername()
                    )
            );
        }

        sendPersonalMessage(
                nextPlayer,
                String.format(
                        "It is your turn to play a card or draw. Here is your hand:\n%s",
                        formatter.formatHand(nextPlayer.getHand())
                ),
                ParseMode.MARKDOWN
        );
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        if (game.isFinished()) {
            game.getParty().getPlayers().forEach(p -> sendPersonalMessage(
                    p,
                    String.format(
                            "@%s played a %s and that was his last card.",
                            p.getUsername(),
                            formatter.formatCard(card)
                    )
            ));

            return;
        }

        String effectText = "";
        switch (card.getType()) {
            case WILD_CHOOSE:
                effectText = String.format("The suit has changed to %s", formatter.formatSuit(chosenSuit));
                break;
            case WILD_DRAW:
                Player nextPlayer = game.getParty().getNext();
                effectText = String.format(
                        "%s has to draw four cards and the suit changed to %s",
                        nextPlayer.getUsername(),
                        formatter.formatSuit(chosenSuit)
                );
                break;
        }

        Player nextPlayer = game.getParty().getNext();
        for (Player p : game.getParty().getPlayers()) {
            sendPersonalMessage(
                    p,
                    String.format(
                            "- @%s played a %s card. %s.\n" +
                                    "- Next player is @%s.\n",
                            player.getUsername(),
                            formatter.formatCard(card),
                            effectText,
                            nextPlayer.getUsername()
                    )
            );
        }

        sendPersonalMessage(
                nextPlayer,
                String.format(
                        "It is your turn to play a card or draw. Here is your hand:\n%s",
                        formatter.formatHand(nextPlayer.getHand())
                ),
                ParseMode.MARKDOWN
        );
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        game.getParty().getPlayers().forEach(p -> {
            sendPersonalMessage(
                    p,
                    String.format(
                            "- @%s drew a card.\n" +
                                    "- Next player is @%s.\n" +
                                    "- Top of discard pile still shows %s.",
                            player.getUsername(),
                            game.getParty().getNext().getUsername(),
                            formatter.formatCard(game.getDiscardPile().getCards().peek())
                    )
            );
        });

        sendPersonalMessage(
                player,
                String.format("You drew a %s card", formatter.formatCard(card))
        );

        Player nextPlayer = game.getParty().getNext();
        sendPersonalMessage(
                nextPlayer,
                String.format(
                        "It is your turn to play a card or draw. Here is your hand:\n%s",
                        formatter.formatHand(nextPlayer.getHand())
                ),
                ParseMode.MARKDOWN
        );
    }

    @Override
    public void gameFinished(UnoGame game) {
        game.getParty().getPlayers().forEach(player -> {
            sendPersonalMessage(
                    player,
                    String.format(
                            "The game has finished and the winner is @%s",
                            game.getWinner().getUsername()
                    )
            );
        });
    }

    private void sendPersonalMessage(Player player, String message) {
        sendPersonalMessage(player, message, "");
    }

    private void sendPersonalMessage(Player player, String message, String parseMode) {
        long chatId = chatMap.get(player).getId();

        try {
            bot.execute(new SendMessage(chatId, message).setParseMode(parseMode));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }
}
