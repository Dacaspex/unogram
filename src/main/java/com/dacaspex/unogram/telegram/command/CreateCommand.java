package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.common.exception.CannotGenerateIdException;
import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.controller.GameControllerFactory;
import com.dacaspex.unogram.controller.announcements.CompoundAnnouncer;
import com.dacaspex.unogram.controller.announcements.ConsoleAnnouncer;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.telegram.TelegramAnnouncer;
import com.dacaspex.unogram.telegram.TelegramPlayerFactory;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public class CreateCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;
    private final GameControllerFactory controllerFactory;
    private final Map<Player, Chat> playerChatMap;
    private final String botUsername;

    public CreateCommand(
            TelegramSender sender,
            PlayerStorage playerStorage,
            GameControllerStorage controllerStorage,
            TelegramPlayerFactory playerFactory,
            GameControllerFactory controllerFactory,
            Map<Player, Chat> playerChatMap,
            String botUsername
    ) {
        this.sender = sender;
        this.playerStorage = playerStorage;
        this.controllerStorage = controllerStorage;
        this.playerFactory = playerFactory;
        this.controllerFactory = controllerFactory;
        this.playerChatMap = playerChatMap;
        this.botUsername = botUsername;
    }

    public void execute(Update update) {
        // Check if the player is already in a game. The player storage contains all
        // currently active players
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player != null) {
            GameController controller = controllerStorage.get(player);

            if (controller == null) {
                throw new IllegalStateException("Controller cannot be null");
            }

            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    String.format("You are already in a game with id %s", controller.getId())
            );

            return;
        }

        // User is not in a game. Make a new player, make the host of a new game
        // and save player
        Player host = playerFactory.create(update);
        playerStorage.add(host);
        playerChatMap.put(host, update.getMessage().getChat());

        GameController controller;
        try {
            controller = controllerFactory.create(
                    new CompoundAnnouncer(
                            new ConsoleAnnouncer(),
                            new TelegramAnnouncer(sender, botUsername)
                    )
            );
        } catch (CannotGenerateIdException e) {
            e.printStackTrace();
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "Could not generate a unique id for the game. Please contact the developer."
            );
            return;
        }

        controllerStorage.add(controller);

        // Setup the game
        controller.create(host);
    }
}
