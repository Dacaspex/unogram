package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.telegram.TelegramPlayerFactory;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class JoinCommand {

    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;
    private final Map<Player, Chat> playerChatMap;

    public JoinCommand(
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
        // Check if the player is already in a game, i.e. it exists in the player storage
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player != null) {
            // Sanity check, player must also exists in the chat map
            if (!playerChatMap.containsKey(player)) {
                // TODO: Better text
                throw new IllegalStateException("Player does not exist in chat map");
            }

            // Get corresponding game (controller) in which the player exists
            GameController controller = controllerStorage.get(player);

            if (controller == null) {
                throw new IllegalStateException("Controller cannot be null");
            }

            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    String.format(
                            "You already joined a game with id %s. You can leave by typing \"leave\"",
                            controller.getId()
                    )
            );
            return;
        }

        // User is not in a game already, create a player
        player = playerFactory.create(update);

        // Decode the game id from the message
        String gameId = decodeGameId(update);
        if (gameId == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "Invalid game id format. Please type \"join &lt;game id&gt;\"."
            );
            return;
        }

        // Check if game id exists
        GameController controller = controllerStorage.get(gameId);
        if (controller == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "There is no game with that game id."
            );
            return;
        }

        // TODO: Check if game is open to join

        // Save the player and let him/her join the game
        playerStorage.add(player);
        playerChatMap.put(player, update.getMessage().getChat());
        controller.join(player);
    }

    private String decodeGameId(Update update) {
        String text = update.getMessage().getText();
        String[] parts = text.split(" ");

        if (parts.length != 2) {
            return null;
        }

        return parts[1];
    }
}
