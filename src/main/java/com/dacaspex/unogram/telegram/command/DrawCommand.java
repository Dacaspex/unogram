package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.telegram.TelegramPlayerFactory;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class DrawCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;
    private final Map<Player, Chat> playerChatMap;

    public DrawCommand(
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
        // Check if the player exists and is in a game
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "You are not in a game. Please join a game by typing <code>/join &lt;game id&gt;</code>"
            );
            return;
        }

        // Get the controller
        GameController controller = controllerStorage.get(player);
        if (controller == null) {
            throw new IllegalStateException("Controller does not exists for player");
        }

        controller.draw(player);

        // If the game is finished, then remove all mappings
        if (controller.isFinished()) {
            // Remove all players
            controller.getParty().getHumans().forEach(p -> {
                playerStorage.remove(player);
                playerChatMap.remove(player);
            });

            // Remove controller
            controllerStorage.remove(controller);
        }
    }
}
