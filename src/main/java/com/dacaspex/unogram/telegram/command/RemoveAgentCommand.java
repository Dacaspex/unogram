package com.dacaspex.unogram.telegram.command;

import com.dacaspex.unogram.ai.Agent;
import com.dacaspex.unogram.common.GameControllerStorage;
import com.dacaspex.unogram.common.PlayerStorage;
import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;
import com.dacaspex.unogram.telegram.TelegramPlayerFactory;
import com.dacaspex.unogram.telegram.TelegramSender;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class RemoveAgentCommand {
    private final TelegramSender sender;
    private final PlayerStorage playerStorage;
    private final GameControllerStorage controllerStorage;
    private final TelegramPlayerFactory playerFactory;

    public RemoveAgentCommand(
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

        // TODO: Remove agent by difficulty

        // Check if there are agents that can be removed
        List<Agent> agents = controller.getParty().getAgents();
        if (agents.size() == 0) {
            sender.sendAndDeleteAfterDelay(
                    update.getMessage().getChat(),
                    "There are no AI players to remove from this party."
            );
            return;
        }

        controller.leave(agents.get(0));
    }
}
