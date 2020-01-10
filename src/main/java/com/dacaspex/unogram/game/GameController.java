package com.dacaspex.unogram.game;

public class GameController {

    private final Announcer announcer;
    private final Party party;
    private UnoGame game;

    public GameController(Announcer announcer) {
        this.announcer = announcer;
        this.party = new Party();
    }

    public boolean join(Player player) {
        if (!party.getPlayers().contains(player)) {
            party.getPlayers().add(player);
            announcer.playerJoinedParty(player);

            return true;
        } else {
            announcer.playerAlreadyInParty(player);

            return false;
        }
    }

    public void leave(Player player) {
        if (party.getPlayers().contains(player)) {
            party.getPlayers().remove(player);
            announcer.playerLeftParty(player);
        } else {
            announcer.playerNotInParty(player);
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
        // Pre-condition: Card cannot be of suit WILD. There is another method
        // that specifically handles that which requires the chosen suit as well
        if (card.getSuit() == Suit.WILD) {
            throw new IllegalArgumentException("Suit cannot be wild");
        }

        // Check if it is the player's turn
        if (!isPlayersTurn(player)) {
            return;
        }

        // Check if the card can be played
        if (!game.canPlay(player, card)) {
            announcer.playedInvalidCard(player, card, game);

            return;
        }

        // Play the card for that player and announce event
        game.play(player, card);
        announcer.playedCard(player, card, game);
    }

    public void play(Player player, Card card, Suit chosenSuit) {
        if (!isPlayersTurn(player)) {
            return;
        }

        if (card.getSuit() != Suit.WILD) {
            throw new IllegalArgumentException("Suit must be of type WILD");
        }

        game.playWild(player, card, chosenSuit);
        announcer.playedWildCard(player, card, chosenSuit, game);
    }

    public void draw(Player player) {
        if (!isPlayersTurn(player)) {
            return;
        }

        Card drawnCard = game.draw(player);
        announcer.drewCard(player, drawnCard, game);
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
