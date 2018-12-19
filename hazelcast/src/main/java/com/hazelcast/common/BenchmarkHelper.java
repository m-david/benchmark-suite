package com.hazelcast.common;

import com.hazelcast.poc.domain.portable.RiskTrade;
import org.openjdk.jmh.infra.Blackhole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static common.BenchmarkConstants.NUMBER_OF_TRADES_TO_PROCESS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * TODO
 *
 * @author Viktor Gamov on 1/6/16.
 *         Twitter: @gamussa
 * @since 0.0.1
 */
public class BenchmarkHelper {
    private static Logger logger = LoggerFactory.getLogger(BenchmarkHelper.class);

    public static void fetchAllRecordsOneByOne(Blackhole blackhole, Map<Integer, RiskTrade> riskTradeCache, Set<Integer> keys) {
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
