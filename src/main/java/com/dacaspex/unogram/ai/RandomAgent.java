package com.dacaspex.unogram.ai;

import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Suit;
import com.dacaspex.unogram.game.UnoGame;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomAgent extends Agent {

    private final Random random;

    public RandomAgent(String id, String username) {
        super(id, username);

        this.random = new Random();
    }

    @Override
    public Move getMove(UnoGame game) {
        // Get possible candidates that are valid moves
        List<Card> candidates = hand.getCards().stream()
                .filter(game::canPlay)
                .collect(Collectors.toList());

        // If there are no valid cards to play, draw a new card
        if (candidates.size() == 0) {
            return Move.createDrawMove();
        }

        Card candidate = candidates.get(random.nextInt(candidates.size()));

        // If the suit is wild, we must also chose a new suit at random
        if (candidate.getSuit() == Suit.WILD) {
            Suit suit = Suit.valuesWithoutWild()[random.nextInt(Suit.valuesWithoutWild().length)];

            return Move.createWildCardMove(candidate, suit);
        }

        return Move.createCardMove(candidate);
    }
}
