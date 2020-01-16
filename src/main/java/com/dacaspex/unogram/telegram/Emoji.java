package com.dacaspex.unogram.telegram;

public enum Emoji {
    RED_HEART("❤"),
    YELLOW_HEART("\uD83D\uDC9B"),
    BLUE_HEART("\uD83D\uDC99"),
    GREEN_HEART("\uD83D\uDC9A"),
    BLACK_HEART("\uD83D\uDDA4");

    private final String id;

    Emoji(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
