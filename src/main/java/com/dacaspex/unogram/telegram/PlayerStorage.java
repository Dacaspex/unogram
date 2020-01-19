package com.dacaspex.unogram.telegram;

import com.dacaspex.unogram.game.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStorage {

    private final Map<String, Player> playerMap;

    public PlayerStorage() {
        this.playerMap = new HashMap<>();
    }

    public void add(Player player) {
        playerMap.put(player.getId(), player);
    }

    public Player get(String id) {
        return playerMap.getOrDefault(id, null);
    }

    public void remove(Player player) {
        playerMap.remove(player.getId());
    }
}
