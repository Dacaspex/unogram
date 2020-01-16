package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.game.*;

import java.util.List;
import java.util.stream.Collectors;

public class TelegramFormatter {

    public String formatHand(Hand hand) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hand.getCards().size(); i++) {
            Card card = hand.getCards().get(i);
            builder.append(String.format("`[%s]` %s\n", i + 1, formatCard(card)));
        }

        return builder.toString();
    }

    public String formatPlayers(List<Player> players) {
        return players.stream().map(Player::getUsername).collect(Collectors.joining(" --> "));
    }

    public String formatCard(Card card) {
        return String.format(
                "%s %s",
                formatSuit(card.getSuit()),
                formatType(card.getType())
        );
    }

    public String formatSuit(Suit suit) {
        switch (suit) {
            case RED:
                return String.format("%s red", Emoji.RED_HEART.toString());
            case YELLOW:
                return String.format("%s yellow", Emoji.YELLOW_HEART.toString());
            case GREEN:
                return String.format("%s green", Emoji.GREEN_HEART.toString());
            case BLUE:
                return String.format("%s blue", Emoji.BLUE_HEART.toString());
            case WILD:
                return String.format("%s wild", Emoji.BLACK_HEART.toString());
            default:
                throw new IllegalArgumentException("Missing case for suit");
        }
    }

    public String formatType(Type type) {
        switch (type) {
            case NUMBER_0:
                return "0";
            case NUMBER_1:
                return "1";
            case NUMBER_2:
                return "2";
            case NUMBER_3:
                return "3";
            case NUMBER_4:
                return "4";
            case NUMBER_5:
                return "5";
            case NUMBER_6:
                return "6";
            case NUMBER_7:
                return "7";
            case NUMBER_8:
                return "8";
            case NUMBER_9:
                return "9";
            case SKIP:
                return "skip";
            case REVERSE:
                return "reverse";
            case DRAW_2:
                return "draw 2";
            case WILD_CHOOSE:
                return "choose suit";
            case WILD_DRAW:
                return "draw 4";
            default:
                throw new IllegalArgumentException("No case for type");
        }
    }

    private String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
