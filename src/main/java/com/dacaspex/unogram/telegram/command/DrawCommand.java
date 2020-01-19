package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.telegram.TelegramPlayerFactory;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public class DrawCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;

    public DrawCommand(
            TelegramSender sender,
            PlayerStorage playerStorage,
            GameControllerStorage controllerStorage,
            TelegramPlayerFactory playerFactory
    ) {
        this.sender = sender;
        this.playerStorage = playerStorage;
        this.controllerStorage = controllerStorage;
        this.playerFactory = playerFactory;
    }

    public void execute(Update update) {
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

        controller.draw(player);
    }
}
