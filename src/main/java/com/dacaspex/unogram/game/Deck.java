package com.dacaspex.unogram.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck {
    private Stack<Card> cards;

    public Deck(Stack<Card> cards) {
        this.cards = cards;
    }

    public Deck() {
        this(new Stack<>());
    }

    public static Deck createStandard() {
        Stack<Card> cards = new Stack<>();

        // Add number cards 0 to 9 for each suit (color)
        for (Type type : Type.valuesWithoutWild()) {
            for (Suit suit : Suit.valuesWithoutWild()) {
                cards.add(new Card(suit, type));
            }
        }

        // Add number cards 1 to 9 for each suit (color)
        for (Type type : Type.valuesWithoutWild()) {
            if (type == Type.NUMBER_0) continue;
            for (Suit suit : Suit.valuesWithoutWild()) {
                cards.add(new Card(suit, type));
            }
        }

        // Add wild cards, 4 of each
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(Suit.WILD, Type.WILD_CHOOSE));
            cards.add(new Card(Suit.WILD, Type.WILD_DRAW));
        }

        return new Deck(cards);
    }

    public Stack<Card> getCards() {
        return cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Hand> deal(int numCards, int numPlayers) {
        List<Hand> hands = new ArrayList<>();

        // Create empty hands
        for (int i = 0; i < numPlayers; i++) {
            hands.add(Hand.createEmpty());
        }

        // Deal cards to each player
        for (int i = 0; i < numCards; i++) {
            for (int j = 0; j < numPlayers; j++) {
                // Add the top card of the stack to the player's hand
                hands.get(j).getCards().add(cards.pop());
            }
        }

        return hands;
    }
}
