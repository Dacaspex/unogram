package com.dacaspex.unogram.main;

import com.dacaspex.unogram.game.Hand;

public class MessageFormatter {

    public String formatHand(Hand hand) {
        StringBuilder builder = new StringBuilder();

        hand.getCards().forEach(card -> builder.append(
                String.format(
                        "- %s %s\n",
                        capitalizeFirst(card.getSuit().name()),
                        card.getType().name()
                )
        ));

        return builder.toString();
    }

    private String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
