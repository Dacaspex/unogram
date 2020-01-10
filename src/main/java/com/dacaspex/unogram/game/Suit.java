package com.dacaspex.unogram.game;

public enum Suit {
    RED,
    YELLOW,
    GREEN,
    BLUE,
    WILD;

    public static Suit[] valuesWithoutWild() {
        return new Suit[]{RED, YELLOW, GREEN, BLUE};
    }
}
