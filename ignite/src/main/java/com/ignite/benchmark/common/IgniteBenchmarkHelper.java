package com.ignite.benchmark.common;

import com.ignite.poc.model.RiskTrade;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static common.BenchmarkConstants.NUMBER_OF_TRADES_TO_PROCESS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class IgniteBenchmarkHelper
{
    private static Logger logger = LoggerFactory.getLogger(IgniteBenchmarkHelper.class);

    public static void fetchAllRecordsOneByOne(IgniteCache<Integer, RiskTrade> riskTradeCache, Set<Integer> keys) {
        long started = System.nanoTime();
        final Set<Integer> set = keys;
        for (Integer key : set) {
            riskTradeCache.get(key);
        }
        long elapsedNanos = System.nanoTime() - started;

        logger.debug("Took {} seconds to get all {} RiskTrades from cache ", NANOSECONDS.toSeconds(elapsedNanos),
                NUMBER_OF_TRADES_TO_PROCESS);
    }

}
