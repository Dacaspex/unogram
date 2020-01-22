package com.dacaspex.unogram.controller;

import com.dacaspex.unogram.ai.Agent;
import com.dacaspex.unogram.ai.Move;
import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.controller.exceptions.DuplicatePlayerException;
import com.dacaspex.unogram.controller.exceptions.InvalidCardException;
import com.dacaspex.unogram.controller.exceptions.PlayerNotInPartyException;
import com.dacaspex.unogram.controller.exceptions.TooManyPlayersException;
import com.dacaspex.unogram.game.*;

public class GameController {

    private final String id;
    private final Announcer announcer;
    private final Party party;
    private final Options options;
    private UnoGame game;
    private boolean abandoned;

    public GameController(String id, Announcer announcer) {
        this.id = id;
        this.announcer = announcer;
        this.options = Options.createStandard();
        this.party = new Party();
        this.game = new UnoGame(party);
        this.abandoned = false;
    }

    public String getId() {
        return id;
    }

    public Party getParty() {
        return party;
    }

    public Options getOptions() {
        return options;
    }

    public boolean isStarted() {
        return game.isStarted();
    }

    public boolean isAbandoned() {
        return abandoned;
    }

    public void create(Player host) {
        party.getPlayers().add(host);
        party.setHost(host);
        announcer.gameCreated(id, host);
    }

    public void join(Player player) {
        // Verify preconditions
        // Cannot add a player that is already in the party
        if (party.getPlayers().contains(player)) {
            throw new DuplicatePlayerException();
        }

        // Cannot add a player if the party is already full
        if (party.getPlayers().size() > options.getMaxNumberOfPlayers()) {
            throw new TooManyPlayersException();
        }

        party.getPlayers().add(player);
        announcer.playerJoinedParty(player, game);
    }

    public void leave(Player player) {
        // Verify preconditions
        // The player must already be in the party in order to be removed
        if (!party.getPlayers().contains(player)) {
            throw new PlayerNotInPartyException();
        }

        // If the host leaves, set the game to be abandoned
        if (player == party.getHost()) {
            abandoned = true;
            announcer.gameAbandoned(game);

            return;
        }

        party.getPlayers().remove(player);
        announcer.playerLeftParty(player, game);
    }

    public void start() {
        // Check game requirements
        if (party.getPlayers().size() > options.getMaxNumberOfPlayers()) {
            throw new TooManyPlayersException();
        }

        // Create the closed and discard piles
        Deck pile = Deck.createStandard();
        Deck discardPile = new Deck();

        // Start the game
        game.start(pile, discardPile);

        // Announce event
        announcer.gameStarted(game);
    }

    public void play(Player player, Card card) {
        play(player, card, null);
    }

    public void play(Player player, Card card, Suit chosenSuit) {
        if (isNotPlayersTurn(player)) {
            return;
        }

        // Check if the card can be played
        if (!game.canPlay(card)) {
            announcer.playedInvalidCard(player, card, game);

            return;
        }

        if (chosenSuit == null) {
            if (card.getSuit() == Suit.WILD) {
                throw new IllegalArgumentException("Suit cannot be wild");
            }

            game.play(player, card);
            party.next();
            announcer.playedCard(player, card, game);
        } else {
            if (card.getSuit() != Suit.WILD) {
                throw new IllegalArgumentException("Suit must be of type WILD");
            }

            game.playWild(player, card, chosenSuit);
            party.next();
            announcer.playedWildCard(player, card, chosenSuit, game);
        }

        // TODO: Event for played last card?

        if (game.isFinished()) {
            announcer.gameFinished(game);
            return;
        }

        handleAgents();
    }

    public void draw(Player player) {
        if (isNotPlayersTurn(player)) {
            return;
        }

        Card drawnCard = game.draw(player);
        party.next();
        announcer.drewCard(player, drawnCard, game);

        handleAgents();
    }

    public boolean isFinished() {
        return game.isFinished();
    }

    private void handleAgents() {
        while (party.getCurrent() instanceof Agent) {
            Agent agent = (Agent) party.getCurrent();
            Move move = agent.getMove(game);

            if (move.isDrawMove()) {
                Card card = game.draw(agent);
                party.next();
                announcer.drewCard(agent, card, game);
            } else {
                if (!game.canPlay(move.getCard())) {
                    throw new InvalidCardException();
                }

                if (move.isCardMove()) {
                    game.play(agent, move.getCard());
                    party.next();
                    announcer.playedCard(agent, move.getCard(), game);
                } else {
                    game.playWild(agent, move.getCard(), move.getSuit());
                    party.next();
                    announcer.playedWildCard(agent, move.getCard(), move.getSuit(), game);
                }
            }

            if (game.isFinished()) {
                announcer.gameFinished(game);
                return;
            }
        }
    }

    private boolean isNotPlayersTurn(Player player) {
        // Check if it is the player's turn
        if (party.getCurrent() != player) {
            announcer.playedBeforeTurn(player, game);

            return true;
        }

        return false;
    }
}
