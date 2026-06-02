package org.core.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Dice {

    public static int roll2d10() {
        return rollD10() + rollD10();
    }

    public static int rollD10() {
        return ThreadLocalRandom.current().nextInt(1, 11);
    }

    public static int rollD(int sides) {
        return ThreadLocalRandom.current().nextInt(1, sides + 1);
    }

    public static int rollD100() {
        return ThreadLocalRandom.current().nextInt(1, 101);
    }
    public static int roll(int times, int sides) {
        int sum = 0;
        for (int i = 0; i < times; i++) {
            sum += rollD(sides);
        }
        return sum;
    }
}

