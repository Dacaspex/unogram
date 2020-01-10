package com.dacaspex.unogram.game;

public class GameController {

    private final Announcer announcer;
    private final Party party;
    private UnoGame game;

    public GameController(Announcer announcer) {
        this.announcer = announcer;
        this.party = new Party();
    }

    public Party getParty() {
        return party;
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
        Deck pile = Deck.createStandard();
        Deck discardPile = new Deck();
        game = new UnoGame(party, pile, discardPile);
        announcer.gameStarted(party, game);
    }

    public void play(Player player, Card card) {
        // TODO: Check turn

        if (card.getSuit() == Suit.WILD) {
            throw new IllegalArgumentException("Suit cannot be wild");
        }

        if (!game.canPlay(player, card)) {
            announcer.playedInvalidCard(player, card, game);

            return;
        }

        game.play(player, card);
        announcer.playedCard(player, card, game);
    }

    public void play(Player player, Card card, Suit chosenSuit) {
        // TODO: Check turn

        if (card.getSuit() != Suit.WILD) {
            throw new IllegalArgumentException("Suit must be of type WILD");
        }

        game.playWild(player, card, chosenSuit);
        announcer.playedWildCard(player, card, chosenSuit, game);
    }

    public void draw(Player player) {
        // TODO: Check turn

        Card drawnCard = game.draw(player);
        announcer.drewCard(player, drawnCard, game);
    }
}
