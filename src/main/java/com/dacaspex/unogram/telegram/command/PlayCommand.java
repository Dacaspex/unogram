package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Card;
import com.dacaspex.unogram.game.Hand;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.game.Suit;
import com.dacaspex.unogram.telegram.GameControllerStorage;
import com.dacaspex.unogram.telegram.PlayerStorage;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class PlayCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;
    private final Map<Player, Chat> playerChatMap;

    public PlayCommand(
            TelegramSender sender,
            PlayerStorage playerStorage,
            GameControllerStorage controllerStorage,
            TelegramPlayerFactory playerFactory,
            Map<Player, Chat> playerChatMap
    ) {
        this.sender = sender;
        this.playerStorage = playerStorage;
        this.controllerStorage = controllerStorage;
        this.playerFactory = playerFactory;
        this.playerChatMap = playerChatMap;
    }

    public void execute(Update update) {
        // TODO: Play before game has started

        // Check if the player exists and is in a game
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "You are not in a game. Please join a game by typing \"join &lt;game id&gt;\""
            );
            return;
        }

        // Get the controller
        GameController controller = controllerStorage.get(player);
        if (controller == null) {
            throw new IllegalStateException("Controller does not exists for player");
        }

        // Decode message into a playable card
        Card card = decodeCard(update, player.getHand());
        if (card == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "Invalid format to play a card. Please write \"play &lt;number&gt;\" or \"play " +
                            "&lt;number&gt; &lt;suit&gt;\" if you want to play a wild card."
            );
            return;
        }

        if (card.getSuit() == Suit.WILD) {
            Suit chosenSuit = decodeChosenSuit(update);
            if (chosenSuit == null) {
                sender.sendAndDeleteAfterDelay(
                        update.getMessage().getChat(),
                        "Invalid format to play a wild card. Write \"play &lt;number&gt; &lt;suit&gt;\" " +
                                "where suit is one of red, yellow, green or blue."
                );
                return;
            }

            controller.play(player, card, chosenSuit);
        } else {
            controller.play(player, card);
        }

        // If the game is finished, then remove all mappings
        if (controller.isFinished()) {
            // Remove all players
            controller.getParty().getPlayers().forEach(p -> {
                playerStorage.remove(player);
                playerChatMap.remove(player);
            });

            // Remove controller
            controllerStorage.remove(controller);
        }
    }

    private Card decodeCard(Update update, Hand hand) {
        String text = update.getMessage().getText();
        String[] parts = text.split(" ");

        // Format must either be "<index>" or "<index> <suit>"
        if (!(parts.length == 2 || parts.length == 3)) {
            return null;
        }

        // Get index of card
        int cardIndex = -1;
        try {
            cardIndex = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }

        // Check if the index is within bounds
        if (cardIndex < 1 || cardIndex > hand.getCards().size()) {
            return null;
        }

        return hand.getCards().get(cardIndex - 1);
    }

    private Suit decodeChosenSuit(Update update) {
        String text = update.getMessage().getText();
        String[] parts = text.split(" ");

        if (parts.length != 3) {
            return null;
        }

        String suitText = parts[2];
        switch (suitText.toLowerCase()) {
            case "red":
                return Suit.RED;
            case "green":
                return Suit.GREEN;
            case "blue":
                return Suit.BLUE;
            case "yellow":
                return Suit.YELLOW;
            default:
                return null;
        }
    }
}
