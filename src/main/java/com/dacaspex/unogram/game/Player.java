package com.dacaspex.unogram.game;

import org.telegram.telegrambots.meta.api.objects.User;

public class Player {
    private final User user;
    private final Hand hand;
    private final long chatId;

    public Player(User user, long chatId) {
        this.user = user;
        this.chatId = chatId;
        this.hand = new Hand();
    }

    public User getUser() {
        return user;
    }

    public Hand getHand() {
        return hand;
    }

    public long getChatId() {
        return chatId;
    }
}
