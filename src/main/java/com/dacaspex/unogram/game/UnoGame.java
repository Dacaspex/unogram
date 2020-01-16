package com.dacaspex.unogram.game;

import java.util.ArrayList;
import java.util.List;

public class UnoGame {

    private final Party party;
    private final Deck pile;
    private final Deck discardPile;
    private Suit chosenSuit;
    private Player winner;

    public UnoGame(Party party, Deck pile, Deck discardPile) {
        this.party = party;
        this.pile = pile;
        this.discardPile = discardPile;
        this.chosenSuit = null;
        this.winner = null;
    }

    public Party getParty() {
        return party;
    }

    public Deck getDiscardPile() {
        return discardPile;
    }

    public Player getWinner() {
        return winner;
    }

    public void start() {
        pile.shuffle();

        List<Hand> newHands = pile.deal(7, party.getPlayers().size());
        for (int i = 0; i < party.getPlayers().size(); i++) {
            Player player = party.getPlayers().get(i);
            Hand hand = newHands.get(i);

            // Add the dealt cards to the player's hand
            player.getHand().getCards().addAll(hand.getCards());
        }

        // Draw first card
        // DEBUG
        while (pile.getCards().peek().getSuit() == Suit.WILD) {
            pile.shuffle();
        }

        discardPile.getCards().push(pile.getCards().pop());
        // TODO: First card action
    }

    public boolean canPlay(Card card) {
        Card topCard = discardPile.getCards().peek();

        return card.getSuit() == topCard.getSuit()
                || card.getType() == topCard.getType()
                || card.getSuit() == Suit.WILD
                || (chosenSuit != null && chosenSuit == card.getSuit());
    }

    public void play(Player player, Card card) {
        if (!canPlay(card)) {
            throw new IllegalArgumentException("Card cannot be played");
        }

        // Put the card from the hand on the discard pile
        player.getHand().getCards().remove(card);
        discardPile.getCards().add(card);

        checkForWinner();

        // A normal card is played, reset the chosen suit
        chosenSuit = null;

        if (card.getType() == Type.REVERSE) {
            party.reverse();
        } else if (card.getType() == Type.SKIP) {
            party.skip();
        } else if (card.getType() == Type.DRAW_2) {
            Player nextPlayer = party.getNext();

            // TODO: Pile could be empty
            // Draw two cards
            draw(nextPlayer, 2);
        }
    }

    public void playWild(Player player, Card card, Suit chosenSuit) {
        if (card.getSuit() != Suit.WILD) {
            throw new IllegalArgumentException("Suit must be of type WILD");
        }

        if (!canPlay(card)) {
            throw new IllegalArgumentException("Card cannot be played");
        }

        // Put the card from the hand on the discard pile
        player.getHand().getCards().remove(card);
        discardPile.getCards().add(card);

        checkForWinner();

        if (card.getType() != Type.WILD_CHOOSE) {
            // Next player should draw four cards
            draw(party.getNext(), 4);
        }

        this.chosenSuit = chosenSuit;
    }

    public Card draw(Player player) {
        // TODO: What if all cards are in the hands of all players?
        if (pile.getCards().size() == 0) {
            // Save the top card and restock the pile
            Card topCard = discardPile.getCards().pop();
            discardPile.getCards().forEach(pile.getCards()::add);
            pile.shuffle();
            discardPile.getCards().push(topCard);
        }

        // Take a card from the pile and add it to the player's hand
        Card card = pile.getCards().pop();
        player.getHand().getCards().add(card);

        return card;
    }

    public List<Card> draw(Player player, int n) {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            cards.add(draw(player));
        }

        return cards;
    }

    public boolean isFinished() {
        return winner != null;
    }

    private void checkForWinner() {
        // There is a winner iff one player has zero cards
        this.winner = party.getPlayers().stream()
                .filter(p -> p.getHand().getCards().isEmpty())
                .findFirst()
                .orElse(null);
    }
}
