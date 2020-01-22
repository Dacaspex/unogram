package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.ai.Agent;
import com.dacaspex.unogram.ai.AgentFactory;
import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.common.exception.CannotGenerateIdException;
import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.telegram.TelegramPlayerFactory;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Update;

public class AddAgentCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;
    private final AgentFactory agentFactory;

    public AddAgentCommand(
            TelegramSender sender,
            PlayerStorage playerStorage,
            GameControllerStorage controllerStorage,
            TelegramPlayerFactory playerFactory,
            AgentFactory agentFactory
    ) {
        this.sender = sender;
        this.playerStorage = playerStorage;
        this.controllerStorage = controllerStorage;
        this.playerFactory = playerFactory;
        this.agentFactory = agentFactory;
    }

    public void execute(Update update) {
        // Check if player exists
        Player player = playerStorage.get(playerFactory.createId(update));
        if (player == null) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "You are not in a game. Please join a game by typing <code>/join &lt;game id&gt;</code>"
            );
            return;
        }

        // Get controller and check requirements
        GameController controller = controllerStorage.get(player);

        // Sanity check, player must be able mappable to a controller
        if (controller == null) {
            throw new IllegalArgumentException("Controller does not exist for player");
        }

        // Only host can add agents
        if (player != controller.getParty().getHost()) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "You cannot add an AI player. Only the host can."
            );
            return;
        }

        // Check availability
        int partySize = controller.getParty().getPlayers().size();
        int maxSize = controller.getOptions().getMaxNumberOfPlayers();
        if (partySize == maxSize) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "The game is already full."
            );
            return;
        }

        // TODO: Get agent difficulty

        try {
            Agent agent = agentFactory.create();
            controller.join(agent);
        } catch (CannotGenerateIdException e) {
            e.printStackTrace(); // TODO...
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "Unable to create a new agent with a unique id. Please inform the developer."
            );
        }
    }
}
