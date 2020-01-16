package com.dacaspex.unogram.controller;

import com.dacaspex.unogram.controller.announcements.Announcer;
import com.dacaspex.unogram.game.*;

public class GameController {

    private final String id;
    private final Announcer announcer;
    private final Party party;
    private UnoGame game;

    public GameController(String id, Announcer announcer) {
        this.id = id;
        this.announcer = announcer;
        this.party = new Party();
    }

    public Party getParty() {
        return party;
    }

    public String getId() {
        return id;
    }

    public void create(Player host) {
        party.getPlayers().add(host);
        party.setHost(host);
        announcer.gameCreated(id, host);
    }

    public boolean join(Player player) {
        // TODO: Don't return boolean
        if (!party.getPlayers().contains(player)) {
            party.getPlayers().add(player);
            announcer.playerJoinedParty(player, party);

            return true;
        } else {
            announcer.playerAlreadyInParty(player, party);

            return false;
        }
    }

    public void leave(Player player) {
        if (party.getPlayers().contains(player)) {
            party.getPlayers().remove(player);
            announcer.playerLeftParty(player, party);
        } else {
            announcer.playerNotInParty(player, party);
        }
    }

    public void start() {
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
            announcer.playedCard(player, card, game);
        } else {
            if (card.getSuit() != Suit.WILD) {
                throw new IllegalArgumentException("Suit must be of type WILD");
            }

            game.playWild(player, card, chosenSuit);
            announcer.playedWildCard(player, card, chosenSuit, game);
        }

        if (game.isFinished()) {
            announcer.gameFinished(game);
        } else {
            party.next();
        }
    }

    public void draw(Player player) {
        if (!isPlayersTurn(player)) {
            return;
        }

        Card drawnCard = game.draw(player);
        announcer.drewCard(player, drawnCard, game);
        party.next();
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
