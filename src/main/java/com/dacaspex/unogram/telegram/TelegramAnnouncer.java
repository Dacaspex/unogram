package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.game.*;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TelegramAnnouncer implements Announcer {

    private final TelegramSender sender;
    private final String gameId;
    private final String botUsername;
    private final TelegramFormatter formatter;

    private Message gameCreatedMessage;
    private Message forwardMessage;
    private Map<Player, Message> joinMessages;
    private Map<Player, Message> gameStateMessages;

    public TelegramAnnouncer(TelegramSender sender, String gameId, String botUsername) {
        this.sender = sender;
        this.gameId = gameId;
        this.botUsername = botUsername;
        this.formatter = new TelegramFormatter();

        this.joinMessages = new HashMap<>();
        this.gameStateMessages = new HashMap<>();
    }

    @Override
    public void gameCreated(String id, Player host) {
        gameCreatedMessage = sender.sendMessage(host, getGameCreatedMessage(id, Collections.singletonList(host)));
        forwardMessage = sender.sendMessage(
                host,
                String.format(
                        "Do you want to play a game of Uno with me? Send a message to @%s with /join %s.",
                        botUsername,
                        id
                )
        );
    }

    @Override
    public void playerJoinedParty(Player player, Party party) {
        // Update host message with the new player that joined
        sender.editMessage(gameCreatedMessage, getGameCreatedMessage(gameId, party.getPlayers()));

        // Update the join message to update the list of players in the game. We do this first
        // to avoid updating the new message we are about to create
        joinMessages.values().forEach(m -> sender.editMessage(m, getJoinedMessage(party.getPlayers())));

        // Send message to player that he joined
        joinMessages.put(player, sender.sendMessage(player, getJoinedMessage(party.getPlayers())));
    }

    @Override
    public void playerLeftParty(Player player, Party party) {
        // Update the message of the player that left
        Message playerJoinMessage = joinMessages.get(player);
        sender.editMessage(playerJoinMessage, "You successfully left the game.");

        // Remove the leave message after some time
        sender.deleteMessageAfterDelay(playerJoinMessage);

        // Update other players' messages to update the player list
        joinMessages.values().forEach(m -> sender.editMessage(m, getJoinedMessage(party.getPlayers())));
    }

    @Override
    public void playerAlreadyInParty(Player player, Party party) {
        // Intentionally left blank
    }

    @Override
    public void playerNotInParty(Player player, Party party) {
        // TODO: Rename method?
        // TODO: Refactor
        sender.sendMessage(player, "You are not yet in the party.");
    }

    @Override
    public void gameStarted(UnoGame game) {
        // Remove host messages
        sender.deleteMessage(gameCreatedMessage);
        sender.deleteMessage(forwardMessage);

        // Remove player joined messages
        joinMessages.values().forEach(sender::deleteMessage);

        // Send and save game status message to everyone
        game.getParty().getPlayers().forEach(player -> {
            Message gameStateMessage = sender.sendMessage(
                    player,
                    getGameStateMessage(game, player, "Game has started!")
            );
            gameStateMessages.put(player, gameStateMessage);
        });
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        // TODO: Explain why
        Message message = sender.sendMessage(player, "That card cannot be played.");
        sender.deleteMessageAfterDelay(message);
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        // TODO: Explain who's turn it is
        Message message = sender.sendMessage(player, "It is not your turn.");
        sender.deleteMessageAfterDelay(message);
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        // Update game state of all players
        game.getParty().getPlayers().forEach(
                p -> sender.editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, formatter.formatLastPlayedCardAction(player, card, game))
                )
        );

        // Notify the next player that has to play a card
        Message message = sender.sendMessage(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
        sender.deleteMessageAfterDelay(message);
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        // Update game state of all players
        game.getParty().getPlayers().forEach(
                p -> sender.editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, formatter.formatLastPlayedCardAction(player, card, game))
                )
        );

        // Notify the next player that has to play a card
        Message message = sender.sendMessage(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
        sender.deleteMessageAfterDelay(message);
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        // Update game state of all players
        game.getParty().getPlayers().forEach(
                p -> sender.editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, String.format("%s drew a card", player.getUsername()))
                )
        );

        // Notify the next player that has to play a card
        Message message = sender.sendMessage(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
        sender.deleteMessageAfterDelay(message);
    }

    @Override
    public void gameFinished(UnoGame game) {
        // TODO: Remove game state messages and replace with finish message?

        game.getParty().getPlayers().forEach(player -> {
            sender.sendMessage(
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
}
