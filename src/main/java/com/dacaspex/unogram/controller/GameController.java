package com.dacaspex.unogram.controller;

import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.controller.exceptions.DuplicatePlayerException;
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

        // Create the game
        game = new UnoGame(party, pile, discardPile);
        game.start();

        // Announce event
        announcer.gameStarted(game);
    }

    public void play(Player player, Card card) {
        play(player, card, null);
    }

    public void play(Player player, Card card, Suit chosenSuit) {
        if (!isPlayersTurn(player)) {
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
        }
    }

    public void draw(Player player) {
        if (!isPlayersTurn(player)) {
            return;
        }

        Card drawnCard = game.draw(player);
        party.next();
        announcer.drewCard(player, drawnCard, game);
    }

    public boolean isFinished() {
        return game.isFinished();
    }

    private boolean isPlayersTurn(Player player) {
        // Check if it is the player's turn
        if (party.getCurrent() != player) {
            announcer.playedBeforeTurn(player, game);

            return false;
        }

        return true;
    }
}
