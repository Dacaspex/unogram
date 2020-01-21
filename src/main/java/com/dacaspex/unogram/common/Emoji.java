package com.dacaspex.unogram.common;

public enum Emoji {
    RED_HEART("❤"),
    YELLOW_HEART("\uD83D\uDC9B"),
    BLUE_HEART("\uD83D\uDC99"),
    GREEN_HEART("\uD83D\uDC9A"),
    BLACK_HEART("\uD83D\uDDA4"),

    JOYSTICK("\uD83D\uDD79️"),

    EXCLAMATION_MARK("❗"),
    INFORMATION("ℹ️"),
    PLAY_BUTTON("▶️"),

    TROPHY("\uD83C\uDFC6"),
    SPORTS_MEDAL("\uD83C\uDFC5");

    private final String unicode;

    Emoji(String unicode) {
        this.unicode = unicode;
    }

    @Override
    public String toString() {
        return unicode;
    }
}
