package com.dacaspex.unogram.game;

import java.util.ArrayList;
import java.util.List;

public class Party {
    private final List<Player> players;
    private Order order;
    private int current;
    private boolean skip;

    public Party() {
        this.players = new ArrayList<>();
        this.order = Order.CLOCKWISE;
        this.current = 0;
        this.skip = false;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrent() {
        return players.get(current);
    }

    public Player getNext() {
        int change = order == Order.CLOCKWISE ? 1 : -1;
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

    public Player next() {
        // Change the index that points to the current player
        int change = order == Order.CLOCKWISE ? 1 : -1;
        current += change;

        // Check if the player should be skipped
        current += skip ? change : 0;
        skip = false;

        current = Math.floorMod(current, players.size());

        return players.get(current);
    }

    public void reverse() {
        order = order == Order.CLOCKWISE ? Order.ANTI_CLOCKWISE : Order.CLOCKWISE;
    }

    public void skip() {
        skip = true;
    }
}
