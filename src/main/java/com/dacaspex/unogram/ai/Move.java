package com.dacaspex.unogram.ai;

import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Suit;

public class Move {
    private final Card card;
    private final Suit suit;
    private final boolean draw;

    public Move(Card card, Suit suit, boolean draw) {
        this.card = card;
        this.suit = suit;
        this.draw = draw;
    }

    public static Move createCardMove(Card card) {
        return new Move(card, null, false);
    }

    public static Move createWildCardMove(Card card, Suit suit) {
        return new Move(card, suit, false);
    }

    public static Move createDrawMove() {
        return new Move(null, null, true);
    }

    public boolean isCardMove() {
        return card != null && suit == null && !draw;
    }

    public boolean isWildCardMove() {
        return card != null && suit != null && !draw;
    }

    public boolean isDrawMove() {
        return card == null && suit == null && draw;
    }

    public Card getCard() {
        return card;
    }

    public Suit getSuit() {
        return suit;
    }
}
