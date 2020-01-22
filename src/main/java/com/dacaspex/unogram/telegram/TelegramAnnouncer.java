package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.ai.Agent;
import com.dacaspex.unogram.common.Emoji;
import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.game.*;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TelegramAnnouncer implements Announcer {
    private final TelegramSender sender;
    private final String botUsername;
    private final TelegramFormatter formatter;

    private String gameId;
    private Message gameCreatedMessage;
    private Message forwardMessage;
    private Map<Player, Message> joinMessages;
    private Map<Player, Message> gameStateMessages;

    public TelegramAnnouncer(TelegramSender sender, String botUsername) {
        this.sender = sender;
        this.botUsername = botUsername;
        this.formatter = new TelegramFormatter();

        this.joinMessages = new HashMap<>();
        this.gameStateMessages = new HashMap<>();
    }

    @Override
    public void gameCreated(String id, Player host) {
        this.gameId = id;
        gameCreatedMessage = sender.sendMessage(host, getGameCreatedMessage(id, Collections.singletonList(host), host));
        forwardMessage = sender.sendMessage(
                host,
                String.format(
                        "Do you want to play a game of Uno with me? Send a message to @%s with <code>/join %s</code>.",
                        botUsername,
                        id
                )
        );
    }

    @Override
    public void playerJoinedParty(Player player, UnoGame game) {
        Party party = game.getParty();

        // Update host message with the new player that joined
        sender.editMessage(gameCreatedMessage, getGameCreatedMessage(gameId, party.getPlayers(), party.getHost()));

        // Update the join message to update the list of players in the game. We do this first
        // to avoid updating the new message we are about to create
        String joinMessage = getJoinedMessage(party.getPlayers(), party.getHost());
        joinMessages.values().forEach(m -> sender.editMessage(m, joinMessage));

        if (isHuman(player)) {
            // Send message to player that he/she joined
            joinMessages.put(player, sender.sendMessage(player, getJoinedMessage(party.getPlayers(), party.getHost())));
        }
    }

    @Override
    public void playerLeftParty(Player player, UnoGame game) {
        Party party = game.getParty();

        // Update host message that the player left
        sender.editMessage(gameCreatedMessage, getGameCreatedMessage(gameId, party.getPlayers(), party.getHost()));

        // Update other players' messages to update the player list
        String joinMessage = getJoinedMessage(party.getPlayers(), party.getHost());
        joinMessages.values().forEach(m -> sender.editMessage(m, joinMessage));

        if (isHuman(player)) {
            // Delete the join message and notify about that the player has left
            sender.deleteMessage(joinMessages.get(player));
            sender.sendAndDeleteAfterDelay(player, "You successfully left the party");
        }
    }

    @Override
    public void gameAbandoned(UnoGame game) {
        if (!game.isStarted()) {
            // Remove host messages
            sender.deleteMessage(gameCreatedMessage);
            sender.deleteMessage(forwardMessage);

            // Remove all join messages that are still there
            joinMessages.values().forEach(sender::deleteMessage);
        } else {
            gameStateMessages.values().forEach(sender::deleteMessage);
        }

        // Notify all players that the game has been abandoned
        game.getParty().getHumans().forEach(p -> sender.sendAndDeleteAfterDelay(
                p,
                "The game has been abandoned and you were kicked from the party"
        ));
    }

    @Override
    public void gameStarted(UnoGame game) {
        // Remove host messages
        sender.deleteMessage(gameCreatedMessage);
        sender.deleteMessage(forwardMessage);

        // Remove player joined messages
        joinMessages.values().forEach(sender::deleteMessage);

        // Send and save game status message to everyone (that is not an agent)
        getHumanPlayers(game).forEach(player -> {
            Message gameStateMessage = sender.sendMessage(
                    player,
                    getGameStateMessage(game, player, "Game has started!")
            );
            gameStateMessages.put(player, gameStateMessage);
        });
    }

    @Override
    public void playedInvalidCard(Player player, Card card, UnoGame game) {
        if (isHuman(player)) {
            // TODO: Explain why
            sender.sendAndDeleteAfterDelay(player, "That card cannot be played.");
        }
    }

    @Override
    public void playedBeforeTurn(Player player, UnoGame game) {
        if (isHuman(player)) {
            // TODO: Explain who's turn it is
            sender.sendAndDeleteAfterDelay(player, "It is not your turn.");
        }
    }

    @Override
    public void playedCard(Player player, Card card, UnoGame game) {
        // Update game state of all players
        getHumanPlayers(game).forEach(
                p -> sender.editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, formatter.formatLastPlayedCardAction(player, card, game))
                )
        );

        if (isHuman(game.getParty().getCurrent())) {
            // Notify the next player that has to play a card
            sender.sendAndDeleteAfterDelay(
                    game.getParty().getCurrent(),
                    "It is your turn to play a card."
            );
        }
    }

    @Override
    public void playedWildCard(Player player, Card card, Suit chosenSuit, UnoGame game) {
        // Update game state of all players
        getHumanPlayers(game).forEach(
                p -> sender.editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, formatter.formatLastPlayedCardAction(player, card, game))
                )
        );

        if (isHuman(game.getParty().getCurrent())) {
            // Notify the next player that has to play a card
            sender.sendAndDeleteAfterDelay(
                    game.getParty().getCurrent(),
                    "It is your turn to play a card."
            );
        }
    }

    @Override
    public void drewCard(Player player, Card card, UnoGame game) {
        // Update game state of all players
        getHumanPlayers(game).forEach(
                p -> sender.editMessage(
                        gameStateMessages.get(p),
                        getGameStateMessage(game, p, String.format("%s drew a card", player.getUsername()))
                )
        );

        if (isHuman(game.getParty().getCurrent())) {
            // Notify the next player that has to play a card
            sender.sendAndDeleteAfterDelay(
                    game.getParty().getCurrent(),
                    "It is your turn to play a card."
            );
        }
    }

    @Override
    public void gameFinished(UnoGame game) {
        getHumanPlayers(game).forEach(player -> {
            sender.deleteMessage(gameStateMessages.get(player));
            sender.sendMessage(player, getFinishedMessage(game));
        });
    }

    private String getGameCreatedMessage(String id, List<Player> players, Player host) {
        return String.format("" +
                        "You successfully created this game with id <code>%s</code>.\n" +
                        "The following players have joined this game:\n" +
                        "%s\n" +
                        "\n" +
                        "You can forward the following message to let them know that they can join your game.",
                id,
                formatter.formatPlayerList(players, host)
        );
    }

    private String getJoinedMessage(List<Player> players, Player host) {
        return String.format(
                "You successfully joined this game of Uno! Your cards and actions will appear in this space. " +
                        "Waiting for the host to start the game. Players in this game:\n%s",
                formatter.formatPlayerList(players, host)
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
                winningPlayers.size() > 0
                        ? formatter.formatWinningPlayers(winningPlayers) + "\n"
                        : "",
                formatter.formatHand(player.getHand(), game)
        );
    }

    private String getFinishedMessage(UnoGame game) {
        String trophyString = String.format("%s%s%s", Emoji.TROPHY, Emoji.SPORTS_MEDAL, Emoji.TROPHY);

        return String.format(
                "%s WINNER %s %s\n",
                trophyString,
                game.getWinner().getUsername(),
                trophyString
        );
    }

    private boolean isHuman(Player player) {
        return !(player instanceof Agent);
    }

    private Stream<Player> getHumanPlayers(UnoGame game) {
        return game.getParty().getPlayers().stream().filter(this::isHuman);
    }
}
