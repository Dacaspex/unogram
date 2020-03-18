package com.dacaspex.unogram.common;

public enum Emoji {
    RED_HEART("❤"),
    YELLOW_HEART("\uD83D\uDC9B"),
    BLUE_HEART("\uD83D\uDC99"),
    GREEN_HEART("\uD83D\uDC9A"),
    BLACK_HEART("\uD83D\uDDA4"),

    GREEN_CIRCLE("\uD83D\uDFE2"),
    RED_CIRCLE("\uD83D\uDD34"),
    YELLOW_CIRCLE("\uD83D\uDFE1"),
    BLUE_CIRCLE("\uD83D\uDD35"),
    BLACK_CIRCLE("⚫"),

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
