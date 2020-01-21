package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.common.Emoji;
import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.game.Suit;
import com.dacaspex.unogram.game.UnoGame;
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
    public void playerJoinedParty(Player player, UnoGame game) {
        // Update host message with the new player that joined
        sender.editMessage(gameCreatedMessage, getGameCreatedMessage(gameId, game.getParty().getPlayers()));

        // Update the join message to update the list of players in the game. We do this first
        // to avoid updating the new message we are about to create
        joinMessages.values().forEach(m -> sender.editMessage(m, getJoinedMessage(game.getParty().getPlayers())));

        // Send message to player that he joined
        joinMessages.put(player, sender.sendMessage(player, getJoinedMessage(game.getParty().getPlayers())));
    }

    @Override
    public void playerLeftParty(Player player, UnoGame game) {
        // Delete the join message and notify about that the player has left
        sender.deleteMessage(joinMessages.get(player));
        sender.sendAndDeleteAfterDelay(player, "You successfully left the party");

        // Update other players' messages to update the player list
        joinMessages.values().forEach(m -> sender.editMessage(m, getJoinedMessage(game.getParty().getPlayers())));
    }

    @Override
    public void gameAbandoned(UnoGame game) {
        // TODO: Implement
        // TODO: Remove all joined messages
        // TODO: Create abandoned messages
        // TODO: Remove host messages
        // TODO: Remove game state messages (if in game)
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
        sender.sendAndDeleteAfterDelay(player, "That card cannot be played.");
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        // TODO: Explain who's turn it is
        sender.sendAndDeleteAfterDelay(player, "It is not your turn.");
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
        sender.sendAndDeleteAfterDelay(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
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
        sender.sendAndDeleteAfterDelay(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
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
        sender.sendAndDeleteAfterDelay(
                game.getParty().getCurrent(),
                "It is your turn to play a card."
        );
    }

    @Override
    public void gameFinished(UnoGame game) {
        game.getParty().getPlayers().forEach(player -> {
            sender.deleteMessage(gameStateMessages.get(player));
            sender.sendAndDeleteAfterDelay(player, getFinishedMessage(game), 30 * 1000);
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
                        "%s" +
                        "\n" +
                        "Your cards:\n%s",
                formatter.formatPlayerOrder(
                        game.getParty().getOrderedPlayers(),
                        game.getParty().getCurrent()
                ),
                formatter.formatDiscardPile(game),
                lastAction,
                game.hasNoCards() ? String.format("%s no cards left in the decks.\n", Emoji.EXCLAMATION_MARK) : "",
                winningPlayers.size() > 0 ? formatter.formatWinningPlayers(winningPlayers) + "\n" : "",
                formatter.formatHand(player.getHand(), game)
        );
    }

    private String getFinishedMessage(UnoGame game) {
        String trophyString = String.format("%s%s%s", Emoji.TROPHY, Emoji.SPORTS_MEDAL, Emoji.TROPHY);

        return String.format(
                "%s WINNER %s %s" +
                        "\n" +
                        "(this message will be deleted after 30 seconds)",
                trophyString,
                game.getWinner().getUsername(),
                trophyString
        );
    }
}
