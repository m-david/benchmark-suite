package com.geode.benchmark.common;

import com.geode.domain.serializable.RiskTrade;
import org.apache.geode.cache.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static common.BenchmarkConstants.NUMBER_OF_TRADES_TO_PROCESS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class GeodeBenchmarkHelper
{
    private static Logger logger = LoggerFactory.getLogger(GeodeBenchmarkHelper.class);

    public static void fetchAllRecordsOneByOne(Map<Integer, RiskTrade> riskTradeCache, Set<Integer> keys) {
        long started = System.nanoTime();
        final Set<Integer> set = keys;
        for (Integer key : set) {
            riskTradeCache.get(key);
        }
        long elapsedNanos = System.nanoTime() - started;

        logger.debug("Took {} seconds to get all {} RiskTrades from cache ", NANOSECONDS.toSeconds(elapsedNanos),
                NUMBER_OF_TRADES_TO_PROCESS);
    }

    public static <K, V> void removeBatch(Region<K, V> region, Set<K> keys, int batchSize)
    {
        Set<K> subset = new HashSet<>();
        Iterator iter = keys.iterator();

        while(iter.hasNext())
        {
            for(int i = 0; i < batchSize && iter.hasNext(); i++)
            {
                subset.add((K)iter.next());
            }
            region.removeAll(subset);
            subset.clear();
        }
        if(subset.size() > 0)
        {
            region.removeAll(subset);
            subset.clear();
        }
    }
}
