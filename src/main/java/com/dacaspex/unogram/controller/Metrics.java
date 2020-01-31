package com.dacaspex.unogram.controller;

import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Suit;

public class Metrics {
    private int nrOfPlayedCards;
    private int nrOfPlayedNormalCards;
    private int nrOfPlayedWildCards;

    public Metrics() {
        this.nrOfPlayedCards = 0;
        this.nrOfPlayedNormalCards = 0;
        this.nrOfPlayedWildCards = 0;
    }

    public int getNrOfPlayedCards() {
        return nrOfPlayedCards;
    }

    public int getNrOfPlayedNormalCards() {
        return nrOfPlayedNormalCards;
    }

    public int getNrOfPlayedWildCards() {
        return nrOfPlayedWildCards;
    }

    public void recordPlayedNormalCard(Card card) {
        if (card.getSuit() == Suit.WILD) {
            throw new IllegalArgumentException("Suit cannot be wild");
        }

        nrOfPlayedCards++;
        nrOfPlayedNormalCards++;
    }

    public void recordPlayedWildCard(Card card) {
        if (card.getSuit() != Suit.WILD) {
            throw new IllegalArgumentException("Suit must be of type wild");
        }

        nrOfPlayedCards++;
        nrOfPlayedWildCards++;
    }
}
