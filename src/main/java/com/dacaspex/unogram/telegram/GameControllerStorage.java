package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.controller.GameController;
import com.dacaspex.unogram.game.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameControllerStorage {

    private final List<GameController> controllers;

    public GameControllerStorage() {
        this.controllers = new ArrayList<>();
    }

    public void add(GameController controller) {
        controllers.add(controller);
    }

    public GameController get(String id) {
        List<GameController> candidates = controllers.stream()
                .filter(c -> c.getId().equals(id))
                .collect(Collectors.toList());

        // TODO: Comment
        // TODO: Exception
        if (candidates.size() > 1) {
            throw new IllegalStateException("Multiple controllers found with same id");
        }

        return controllers.size() == 1 ? controllers.get(0) : null;
    }

    public GameController get(Player player) {
        List<GameController> candidates = controllers.stream()
                .filter(c -> c.getParty().getPlayers().contains(player))
                .collect(Collectors.toList());

        // TODO: Comment
        // TODO: Exception
        if (candidates.size() > 1) {
            throw new IllegalStateException("Multiple controllers found with same player");
        }

        return controllers.size() == 1 ? controllers.get(0) : null;
    }

    public void remove(GameController controller) {
        controllers.remove(controller);
    }
}
