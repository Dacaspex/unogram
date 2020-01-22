package com.dacaspex.unogram.ai;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AgentNameFactory {
    private final static String prefix;
    private final static List<String> candidates;

    static {
        // TODO: Extend with more names
        prefix = "[AI]";
        candidates = Arrays.asList(
                "Skynet",
                "BottyMcBotFace",
                "TheLegend27",
                "Dron",
                "John",
                "Robert",
                "Brian",
                "James",
                "Christopher",
                "Jeffrey",
                "Horacio",
                "Walter",
                "Arthur",
                "Alberto",
                "Alenka",
                "Finn",
                "Jana",
                "Chad",
                "Mila",
                "Alfred",
                "Jackie",
                "Paulina"
        );
    }

    private final Random random;

    public AgentNameFactory() {
        this.random = new Random();
    }

    public String create() {
        return String.format(
                "%s %s",
                prefix,
                candidates.get(random.nextInt(candidates.size()))
        );
    }
}
