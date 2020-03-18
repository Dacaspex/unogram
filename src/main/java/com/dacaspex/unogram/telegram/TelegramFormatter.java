package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.common.Emoji;
import com.dacaspex.unogram.game.*;

import java.util.List;
import java.util.stream.Collectors;

public class TelegramFormatter {

    // TODO: Card can be null

    public String formatHand(Hand hand, UnoGame game) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hand.getCards().size(); i++) {
            Card card = hand.getCards().get(i);
            if (game.canPlay(card)) {
                builder.append(String.format("<code>[%s]</code> %s\n", i + 1, formatCard(card)));
            } else {
                builder.append(String.format("<s><code>[%s]</code> %s</s>\n", i + 1, formatCard(card)));
            }
        }

        return builder.toString();
    }

    public String formatPlayerOrder(List<Player> orderedPlayers, Player currentPlayer) {
        return orderedPlayers.stream()
                .map(p -> p == currentPlayer
                        ? String.format("<u>%s</u>", p.getUsername())
                        : p.getUsername()
                )
                .collect(Collectors.joining(" <code>--></code> "));
    }

    public String formatPlayerList(List<Player> players, Player host) {
        return players.stream()
                .map(p -> String.format(
                        "- %s %s",
                        p.getUsername(),
                        p == host ? "(host)" : ""
                ))
                .collect(Collectors.joining("\n"));
    }

    public String formatWinningPlayers(List<Player> winningPlayers) {
        return winningPlayers.stream()
                .map(p -> String.format("%s %s has one card left.", Emoji.EXCLAMATION_MARK, p.getUsername()))
                .collect(Collectors.joining("\n"));
    }

    public String formatDiscardPile(UnoGame game) {
        Card topCard = game.getDiscardPile().getCards().peek();

        return game.getChosenSuit() == null
                ? formatCard(topCard)
                : String.format("%s (Suit changed to %s", formatCard(topCard), formatSuit(game.getChosenSuit()));
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
                return String.format("%s red", Emoji.RED_CIRCLE.toString());
            case YELLOW:
                return String.format("%s yellow", Emoji.YELLOW_CIRCLE.toString());
            case GREEN:
                return String.format("%s green", Emoji.GREEN_CIRCLE.toString());
            case BLUE:
                return String.format("%s blue", Emoji.BLUE_CIRCLE.toString());
            case WILD:
                return String.format("%s wild", Emoji.BLACK_CIRCLE.toString());
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

    public String formatLastPlayedCardAction(Player player, Card card, UnoGame game) {
        switch (card.getType()) {
            case NUMBER_0:
            case NUMBER_1:
            case NUMBER_2:
            case NUMBER_3:
            case NUMBER_4:
            case NUMBER_5:
            case NUMBER_6:
            case NUMBER_7:
            case NUMBER_8:
            case NUMBER_9:
                return String.format("%s played a card.", player.getUsername());
            case SKIP:
                return String.format(
                        "%s was skipped by %s.",
                        game.getParty().getPrevious(1).getUsername(),
                        game.getParty().getPrevious(2).getUsername()
                );
            case REVERSE:
                if (game.getParty().getPlayers().size() == 2) {
                    // Special rule in 2-player games that a reverse cards acts as a skip card
                    return String.format(
                            "%s was skipped by %s",
                            game.getParty().getPrevious(1).getUsername(),
                            game.getParty().getPrevious(2).getUsername()
                    );
                } else {
                    return String.format(
                            "The order is now reversed by %s",
                            game.getParty().getPrevious().getUsername()
                    );
                }
            case DRAW_2:
                return String.format(
                        "%s caused %s to draw 2 cards.",
                        game.getParty().getPrevious().getUsername(),
                        game.getParty().getCurrent().getUsername()
                );
            case WILD_CHOOSE:
                return String.format(
                        "%s changed the suit to %s",
                        game.getParty().getPrevious().getUsername(),
                        formatSuit(game.getChosenSuit())
                );
            case WILD_DRAW:
                return String.format(
                        "%s caused %s to draw 4 cards and changed the suit to %s",
                        game.getParty().getPrevious().getUsername(),
                        game.getParty().getCurrent().getUsername(),
                        formatSuit(game.getChosenSuit())
                );
            default:
                throw new IllegalArgumentException("No formatting for type");
        }
    }
}
