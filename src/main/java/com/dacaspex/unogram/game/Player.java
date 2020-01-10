package com.dacaspex.unogram.game;

import org.telegram.telegrambots.meta.api.objects.User;

public class Player {
    private final String username;
    private final Hand hand;

    public Player(String username) {
        this.username = username;
        this.hand = new Hand();
    }

    public Hand getHand() {
        return hand;
    }

    public String getUsername() {
        return username;
    }
}
