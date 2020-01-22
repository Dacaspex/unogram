package com.dacaspex.unogram.common;

import java.util.Random;

public class IdGenerator {
    public static final String alphabet;
    public static final String digits;
    public static final String alphanumeric;

    static {
        alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        digits = "0123456789";
        alphanumeric = alphabet + digits;
    }

    private final Random random;
    private final char[] symbols;

    public IdGenerator(String symbols) {
        this.random = new Random();
        this.symbols = symbols.toCharArray();
    }

    public IdGenerator() {
        this(alphanumeric);
    }

    public String generate(int length) {
        return generate("", length);
    }

    public String generate(String prefix, int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length cannot be less than 1");
        }

        char[] buffer = new char[length];

        for (int i = 0; i < length; i++) {
            buffer[i] = symbols[random.nextInt(symbols.length)];
        }

        return prefix + new String(buffer);
    }
}
