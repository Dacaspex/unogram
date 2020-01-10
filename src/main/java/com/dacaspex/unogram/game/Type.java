package com.dacaspex.unogram.game;

public enum Type {
    NUMBER_0,
    NUMBER_1,
    NUMBER_2,
    NUMBER_3,
    NUMBER_4,
    NUMBER_5,
    NUMBER_6,
    NUMBER_7,
    NUMBER_8,
    NUMBER_9,
    SKIP,
    REVERSE,
    DRAW_2,
    WILD_CHOOSE,
    WILD_DRAW;

    public static Type[] valuesWithoutWild() {
        return new Type[]{
                NUMBER_0,
                NUMBER_1,
                NUMBER_2,
                NUMBER_3,
                NUMBER_4,
                NUMBER_5,
                NUMBER_6,
                NUMBER_7,
                NUMBER_8,
                NUMBER_9,
                SKIP,
                REVERSE,
                DRAW_2,
        };
    }
}
