package com.dacaspex.unogram.game;

public class Player {
    private final String id;
    private final String username;
    private final Hand hand;

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
