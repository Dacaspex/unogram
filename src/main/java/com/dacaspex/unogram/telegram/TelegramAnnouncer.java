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
    private Map<Player, Chat> chatMap;
    private Timer timer;

    private Message gameCreatedMessage;
    private Message forwardMessage;
    private Map<Player, Message> joinMessages;
    private Map<Player, Message> gameStateMessages;

    public TelegramAnnouncer(TelegramLongPollingBot bot, String gameId) {
        this.bot = bot;
        this.gameId = gameId;
        this.chatMap = new HashMap<>();
        this.timer = new Timer();
        this.formatter = new TelegramFormatter();
        this.joinMessages = new HashMap<>();
        this.gameStateMessages = new HashMap<>();
    }

    public void addUserMapping(User user, Player player, Chat chat) {
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
        deleteMessageAfterDelay(playerJoinMessage, 5000);

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
            Message gameStateMessage = sendMessage(
                    player,
                    getGameStateMessage(game, player, "Game has started!")
            );
            gameStateMessages.put(player, gameStateMessage);
        });
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        // TODO: Explain why
        Message message = sendMessage(player, "That card cannot be played.");
        deleteMessageAfterDelay(message, 5000);
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        // TODO: Explain who's turn it is
        Message message = sendMessage(player, "It is not your turn.");
        deleteMessageAfterDelay(message, 5000);
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        // Update game state of all players
        game.getParty().getPlayers().forEach(
                p -> editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, formatter.formatLastPlayedCardAction(player, card, game))
                )
        );

        // Notify the next player that has to play a card
        Message message = sendMessage(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
        deleteMessageAfterDelay(message, 5000);
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        // Update game state of all players
        game.getParty().getPlayers().forEach(
                p -> editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, formatter.formatLastPlayedCardAction(player, card, game))
                )
        );

        // Notify the next player that has to play a card
        Message message = sendMessage(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
        deleteMessageAfterDelay(message, 5000);
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        // Update game state of all players
        game.getParty().getPlayers().forEach(
                p -> editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, String.format("%s drew a card", player.getUsername()))
                )
        );

        // Notify the next player that has to play a card
        Message message = sendMessage(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
        deleteMessageAfterDelay(message, 5000);
    }

    @Override
    public void gameFinished(UnoGame game) {
        // TODO: Remove game state messages and replace with finish message?

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

    private String getGameStateMessage(UnoGame game, Player player, String lastAction) {
        List<Player> winningPlayers = game.getParty().getPlayers().stream()
                .filter(p -> p.getHand().getCards().size() == 1)
                .collect(Collectors.toList());

        return String.format("" +
                        "Order: %s\n" +
                        "\n" +
                        "Discard pile: %s\n" +
                        "Last action: %s\n" +
                        "%s" +
                        "\n" +
                        "Your cards:\n%s",
                formatter.formatPlayerOrder(
                        game.getParty().getOrderedPlayers(),
                        game.getParty().getCurrent()
                ),
                formatter.formatDiscardPile(game),
                lastAction,
                winningPlayers.size() > 0 ? formatter.formatWinningPlayers(winningPlayers) + "\n" : "",
                formatter.formatHand(player.getHand(), game)
        );
    }

    private Message sendMessage(Player player, String message) {
        long chatId = chatMap.get(player).getId();

        try {
            return bot.execute(new SendMessage(chatId, message).setParseMode(ParseMode.HTML));
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
                            .setParseMode(ParseMode.HTML)
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

    private void deleteMessageAfterDelay(Message message, int delay) {
        timer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        deleteMessage(message);
                    }
                },
                delay
        );
    }
}
