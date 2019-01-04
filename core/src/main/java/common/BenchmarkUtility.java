package common;

import java.util.concurrent.ThreadLocalRandom;

public class BenchmarkUtility
{

    public static int getRandomStartIndex(int bounds)
    {
        return (int) ThreadLocalRandom.current().nextDouble() * (bounds-1);
    }

    public static int getRandom(int origin, int bounds)
    {
        return ThreadLocalRandom.current().nextInt(origin, bounds);
    }
}
