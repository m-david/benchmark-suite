package common;

import java.util.concurrent.ThreadLocalRandom;

import static common.BenchmarkConstants.NUMBER_OF_TRADES_TO_PROCESS;

public class BenchmarkUtility
{

    public static int getRandomStartIndex(int bounds)
    {
        return getRandom(0, (bounds-1));
    }

    public static int getRandom()
    {
        return ThreadLocalRandom.current().nextInt(0, NUMBER_OF_TRADES_TO_PROCESS);
    }

    public static int getRandom(int origin, int bounds)
    {
        return ThreadLocalRandom.current().nextInt(origin, bounds);
    }

}
