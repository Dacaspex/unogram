package com.dacaspex.unogram.common;

public enum Emoji {
    RED_HEART("❤"),
    YELLOW_HEART("\uD83D\uDC9B"),
    BLUE_HEART("\uD83D\uDC99"),
    GREEN_HEART("\uD83D\uDC9A"),
    BLACK_HEART("\uD83D\uDDA4"),

    EXCLAMATION_MARK("❗");

    private final String unicode;

    Emoji(String unicode) {
        this.unicode = unicode;
    }

    @Override
    public String toString() {
        return unicode;
    }
}
