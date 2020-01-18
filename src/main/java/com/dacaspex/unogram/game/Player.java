package com.dacaspex.unogram.game;

public class Player {
    private final String username;
    private final String platformSource;
    private final Hand hand;

    public Player(String username, String platformSource) {
        this.username = username;
        this.platformSource = platformSource;
        this.hand = new Hand();
    }

    public String getUsername() {
        return username;
    }

    public String getPlatformSource() {
        return platformSource;
    }

    public Hand getHand() {
        return hand;
    }
}
