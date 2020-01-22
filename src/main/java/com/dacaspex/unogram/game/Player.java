package com.dacaspex.unogram.game;

public class Player {
    protected final String id;
    protected final String username;
    protected final Hand hand;

    public Player(String id, String username) {
        this.id = id;
        this.username = username;
        this.hand = new Hand();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Hand getHand() {
        return hand;
    }
}
