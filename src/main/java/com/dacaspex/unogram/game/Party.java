package com.dacaspex.unogram.game;

import com.dacaspex.unogram.ai.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Party {
    private final List<Player> players;
    private Player host;
    private Order order;
    private int current;
    private boolean skip;

    public Party() {
        this.players = new ArrayList<>();
        this.host = null;
        this.order = Order.CLOCKWISE;
        this.current = 0;
        this.skip = false;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getHumans() {
        return players.stream()
                .filter(p -> !(p instanceof Agent))
                .collect(Collectors.toList());
    }

    public List<Agent> getAgents() {
        return players.stream()
                .filter(p -> p instanceof Agent)
                .map(p -> (Agent) p)
                .collect(Collectors.toList());
    }

    public Player getHost() {
        return host;
    }

    public void setHost(Player host) {
        this.host = host;
    }

    public List<Player> getOrderedPlayers() {
        List<Player> orderedPlayers = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            orderedPlayers.add(getNext(i));
        }

        return orderedPlayers;
    }

    public Player getCurrent() {
        return players.get(current);
    }

    public Player getNext() {
        return getNext(1);
    }

    public Player getNext(int n) {
        n += skip ? 1 : 0;
        int change = order == Order.CLOCKWISE ? n : -n;
        int index = Math.floorMod(current + change, players.size());

        return players.get(index);
    }

    public Player getPrevious() {
        return getPrevious(1);
    }

    public Player getPrevious(int n) {
        int change = order == Order.CLOCKWISE ? -n : n;
        int index = Math.floorMod(current + change, players.size());

        return players.get(index);
    }

    public void next() {
        // Change the index that points to the current player
        int change = order == Order.CLOCKWISE ? 1 : -1;
        current += change;

        // Check if the player should be skipped
        current += skip ? change : 0;
        skip = false;

        current = Math.floorMod(current, players.size());
    }

    public void reverse() {
        order = order == Order.CLOCKWISE ? Order.ANTI_CLOCKWISE : Order.CLOCKWISE;
    }

    public void skip() {
        skip = true;
    }
}
