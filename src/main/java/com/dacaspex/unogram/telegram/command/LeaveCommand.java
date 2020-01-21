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

public class LeaveCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;
    private final Map<Player, Chat> playerChatMap;

    public LeaveCommand(
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
        // Check if the player is not in a game, i.e. it does not exist in the player storage
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player == null) {
            sender.sendAndDeleteAfterDelay(update.getMessage().getChat(), "You are not in a game yet.");

            return;
        }

        // Sanity check, player must also exist in the player chat map
        if (!playerChatMap.containsKey(player)) {
            throw new IllegalStateException("Player does not exist in chat map");
        }

        // Get the controller in which the player resides
        GameController controller = controllerStorage.get(player);
        if (controller == null) {
            throw new IllegalStateException("Controller cannot be null");
        }

        controller.leave(player);
        if (controller.isAbandoned()) {
            // The host left the game, remove all players from the storage
            controller.getParty().getPlayers().forEach(p -> {
                playerStorage.remove(player);
                playerChatMap.remove(player);
            });

            // Remove controller
            controllerStorage.remove(controller);
        } else {
            // Only remove the specific player
            playerStorage.remove(player);
            playerChatMap.remove(player);
        }
    }
}
