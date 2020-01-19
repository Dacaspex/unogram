package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.telegram.GameControllerStorage;
import com.dacaspex.unogram.telegram.PlayerStorage;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public class StartCommand {

    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;

    public StartCommand(
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
        // Check if player exists
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "You are not in a game. Please join a game by typing \"join &lt;game id&gt;\""
            );
            return;
        }

        // Get controller and check requirements
        GameController controller = controllerStorage.get(player);

        // Sanity check, player must be able mappable to a controller
        if (controller == null) {
            throw new IllegalArgumentException("Controller does not exist for player");
        }

        // Check if the player is the host
        if (controller.getParty().getHost() != player) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    String.format(
                            "You cannot start this game because you are not the host. %s is the host.",
                            controller.getParty().getHost().getUsername()
                    )
            );
            return;
        }

        // Minimum amount of players
        // TODO: Minimum amount of players

        controller.start();
    }
}
