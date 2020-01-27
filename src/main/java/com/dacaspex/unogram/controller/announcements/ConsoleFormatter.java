package com.dacaspex.unogram.controller.announcements;

import com.dacaspex.unogram.game.Card;

public class ConsoleFormatter {

    public String formatCard(Card card) {
        if (card == null) {
            return "(no card)";
        } else {
            return String.format("{suit: %s, type: %s}", card.getSuit().name(), card.getType().name());
        }
    }
}
