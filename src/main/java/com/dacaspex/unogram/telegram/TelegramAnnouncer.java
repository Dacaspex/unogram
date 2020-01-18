package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.game.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.Collectors;

public class TelegramAnnouncer implements Announcer {

    private final TelegramLongPollingBot bot;
    private final String gameId;
    private final TelegramFormatter formatter;
    private Map<Player, User> userMap;
    private Map<Player, Chat> chatMap;
    private Timer timer;

    private Message gameCreatedMessage;
    private Message forwardMessage;
    private Map<Player, Message> joinMessages;
    private Map<Player, Message> gameStateMessages;

    public TelegramAnnouncer(TelegramLongPollingBot bot, String gameId) {
        this.bot = bot;
        this.gameId = gameId;
        this.userMap = new HashMap<>();
        this.chatMap = new HashMap<>();
        this.timer = new Timer();
        this.formatter = new TelegramFormatter();
        this.joinMessages = new HashMap<>();
        this.gameStateMessages = new HashMap<>();
    }

    public void addUserMapping(User user, Player player, Chat chat) {
        userMap.put(player, user);
        chatMap.put(player, chat);
    }

    @Override
    public void gameCreated(String id, Player host) {
        gameCreatedMessage = sendMessage(host, getGameCreatedMessage(id, Collections.singletonList(host)));
        forwardMessage = sendMessage(
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
        // Update host message with the new player that joined
        editMessage(gameCreatedMessage, getGameCreatedMessage(gameId, party.getPlayers()));

        // Update the join message to update the list of players in the game. We do this first
        // to avoid updating the new message we are about to create
        joinMessages.values().forEach(m -> editMessage(m, getJoinedMessage(party.getPlayers())));

        // Send message to player that he joined
        joinMessages.put(player, sendMessage(player, getJoinedMessage(party.getPlayers())));
    }

    @Override
    public void playerLeftParty(Player player, Party party) {
        // Update the message of the player that left
        Message playerJoinMessage = joinMessages.get(player);
        editMessage(playerJoinMessage, "You successfully left the game.");

        // Remove the leave message after some time
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        deleteMessage(playerJoinMessage);
                    }
                },
                5000
        );

        // Update other players' messages to update the player list
        joinMessages.values().forEach(m -> editMessage(m, getJoinedMessage(party.getPlayers())));
    }

    @Override
    public void playerAlreadyInParty(Player player, Party party) {
        // Intentionally left blank
    }

    @Override
    public void playerNotInParty(Player player, Party party) {
        // TODO: Rename method?
        // TODO: Refactor
        sendMessage(player, "You are not yet in the party.");
    }

    @Override
    public void gameStarted(UnoGame game) {
        // Remove host messages
        deleteMessage(gameCreatedMessage);
        deleteMessage(forwardMessage);

        // Remove player joined messages
        joinMessages.values().forEach(this::deleteMessage);

        // Send and save game status message to everyone
        game.getParty().getPlayers().forEach(player -> {
            Message gameStateMessage = sendMessage(player, getGameStateMessage(game, player));
            gameStateMessages.put(player, gameStateMessage);
        });
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        // TODO: Explain why
        sendMessage(player, "That card cannot be played.");

        // TODO: Send and save message
        // TODO: Delete message when player played a correct card
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        // TODO: Explain who's turn it is
        sendMessage(player, "It is not your turn.");

        // TODO: Revamp
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        if (game.isFinished()) {
            game.getParty().getPlayers().forEach(p -> sendMessage(
                    p,
                    String.format(
                            "@%s played a %s and that was his last card.",
                            player.getUsername(),
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
            sendMessage(
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

        sendMessage(
                nextPlayer,
                String.format(
                        "It is your turn to play a card or draw. Here is your hand:\n%s",
                        formatter.formatHand(nextPlayer.getHand(), game)
                ),
                ParseMode.HTML
        );
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        if (game.isFinished()) {
            game.getParty().getPlayers().forEach(p -> sendMessage(
                    p,
                    String.format(
                            "@%s played a %s and that was his last card.",
                            player.getUsername(),
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
            sendMessage(
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

        sendMessage(
                nextPlayer,
                String.format(
                        "It is your turn to play a card or draw. Here is your hand:\n%s",
                        formatter.formatHand(nextPlayer.getHand(), game)
                ),
                ParseMode.HTML
        );
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        game.getParty().getPlayers().forEach(p -> {
            sendMessage(
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

        sendMessage(
                player,
                String.format("You drew a %s card", formatter.formatCard(card))
        );

        Player nextPlayer = game.getParty().getNext();
        sendMessage(
                nextPlayer,
                String.format(
                        "It is your turn to play a card or draw. Here is your hand:\n%s",
                        formatter.formatHand(nextPlayer.getHand(), game)
                ),
                ParseMode.MARKDOWN
        );
    }

    @Override
    public void gameFinished(UnoGame game) {
        game.getParty().getPlayers().forEach(player -> {
            sendMessage(
                    player,
                    String.format(
                            "The game has finished and the winner is @%s",
                            game.getWinner().getUsername()
                    )
            );
        });
    }

    private String getGameCreatedMessage(String id, List<Player> players) {
        return String.format("" +
                        "You successfully created this game with id %s.\n" +
                        "The following players have joined this game:\n" +
                        "%s\n" +
                        "\n" +
                        "You can forward the following message to let them know that they can join your game.",
                id,
                formatter.formatPlayerList(players)
        );
    }

    private String getJoinedMessage(List<Player> players) {
        return String.format(
                "You successfully joined this game of Uno! Your cards and actions will appear in this space. " +
                        "Waiting for the host to start the game. Players in this game:\n%s",
                formatter.formatPlayerList(players)
        );
    }

    private String getGameStateMessage(UnoGame game, Player player) {
        Card topCard = game.getDiscardPile().getCards().peek();
        List<Player> winningPlayers = game.getParty().getPlayers().stream()
                .filter(p -> p.getHand().getCards().size() == 1)
                .collect(Collectors.toList());

        return String.format("" +
                        "Order: %s\n" +
                        "\n" +
                        "Discard pile: %s\n" +
                        "%s" +
                        "Your cards:\n%s",
                formatter.formatPlayerOrder(
                        game.getParty().getOrderedPlayers(),
                        game.getParty().getCurrent()
                ),
                formatter.formatCard(topCard),
                formatter.formatWinningPlayers(winningPlayers),
                formatter.formatHand(player.getHand(), game)
        );
    }

    private Message sendMessage(Player player, String message) {
        return sendMessage(player, message, ParseMode.HTML);
    }

    private Message sendMessage(Player player, String message, String parseMode) {
        long chatId = chatMap.get(player).getId();

        // TODO: Remove message

        try {
            return bot.execute(new SendMessage(chatId, message).setParseMode(parseMode));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
            return null;
        }
    }

    private void editMessage(Message message, String text) {
        try {
            bot.execute(
                    new EditMessageText()
                            .setChatId(message.getChatId())
                            .setMessageId(message.getMessageId())
                            .setText(text)
            );
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }

    private void deleteMessage(Message message) {
        try {
            bot.execute(new DeleteMessage(message.getChatId(), message.getMessageId()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            // TODO
        }
    }
}
