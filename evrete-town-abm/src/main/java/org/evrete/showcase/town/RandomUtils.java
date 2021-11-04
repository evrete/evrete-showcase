package org.evrete.showcase.town;

import java.security.SecureRandom;
import java.util.List;

public final class RandomUtils {
    private final static SecureRandom random = new SecureRandom();

    public static boolean randomBoolean(double probability) {
        return random.nextDouble() < probability;
    }

    public static int positiveRandom(int mean, int stdDev, int maximum) {
        int rand = positiveRandom(mean, stdDev);
        if (rand > maximum) {
            return positiveRandom(mean, stdDev, maximum);
        } else {
            return rand;
        }
    }

    public static int positiveRandom(int mean, int stdDev) {
        double rand = random.nextGaussian() * stdDev + mean;
        // The below is not entirely correct, but it's fine for our purposes
        return rand < 0 ? positiveRandom(mean, stdDev) : (int) rand;
    }

    public static int random(int range) {
        return random.nextInt(range);
    }

    static double nextDouble() {
        return random.nextDouble();
    }

    public static <T> T randomListElement(List<T> data) {
        return data.get(random.nextInt(data.size()));
    }
}
