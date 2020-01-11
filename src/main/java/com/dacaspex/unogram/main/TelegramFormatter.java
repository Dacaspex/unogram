package com.dacaspex.unogram.main;

import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Hand;
import com.dacaspex.unogram.game.Player;

import java.util.List;
import java.util.stream.Collectors;

public class TelegramFormatter {

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

    public String formatPlayers(List<Player> players) {
        return players.stream().map(Player::toString).collect(Collectors.joining(", "));
    }

    public String formatCard(Card card) {
        return card.toString();
    }

    private String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
