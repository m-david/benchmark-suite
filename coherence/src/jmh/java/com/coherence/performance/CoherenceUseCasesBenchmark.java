package com.coherence.performance;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.EqualsFilter;
import common.domain.RiskTrade;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.coherence.common.CoherenceBenchmarkHelper.getMeDummyRiskTrades;
import static common.BenchmarkConstants.*;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.AverageTime})
public class CoherenceUseCasesBenchmark {

    private static Logger logger = LoggerFactory.getLogger(CoherenceUseCasesBenchmark.class);


    @State(Scope.Thread)
    public static class InitReadCacheState
    {
        private NamedCache<Integer, RiskTrade> riskTradeReadCache;
        private NamedCache<Integer, RiskTrade> riskTradeOffHeapCache;

        private List<RiskTrade> riskTradeList;

        @Setup(Level.Trial)
        public void before()
        {
            riskTradeReadCache = CacheFactory.getCache(TRADE_READ_MAP);
            riskTradeOffHeapCache = CacheFactory.getCache(TRADE_OFFHEAP_MAP);
            riskTradeList = getMeDummyRiskTrades();
            putRiskTrades(riskTradeReadCache, riskTradeList);
        }

        @TearDown(Level.Trial)
        public void afterAll()
        {
            CacheFactory.shutdown();
        }
    }

    //region FIXTURE
    @Benchmark
    public void b01_InsertTradesSingle(InitReadCacheState state) throws Exception {

        putRiskTrades(state.riskTradeOffHeapCache, state.riskTradeList);

    } // 164 seconds for 100000

    @Benchmark
    public void b02_InsertTradesBulk1000(InitReadCacheState state) throws Exception
    {
        persistAllRiskTradesIntoCacheInOneGo(state.riskTradeOffHeapCache, state.riskTradeList, BATCH_SIZE);
    }

    @Benchmark
    public void b03_GetAllRiskTradesSingle(InitReadCacheState state) throws Exception {

        fetchAllRecordsOneByOne(state.riskTradeReadCache);
    } // 131 seconds for 100000

    @Benchmark
    public void b04_GetTradeOneFilter(InitReadCacheState state) throws Exception {
        ValueExtractor valueExtractor = new PofExtractor(null, 10);
        Filter filter = new EqualsFilter(valueExtractor, "USD");

        Set<Integer> allRiskTradesWhereCurrencyIsUsd = state.riskTradeReadCache.keySet(filter);
        allRiskTradesWhereCurrencyIsUsd.forEach(key -> state.riskTradeReadCache.get(key));
    } // 66 seconds to apply filter and get 50,000 records out of 100000

    @Benchmark
    public void b05_GetTradeThreeFilter(InitReadCacheState state) throws Exception {

        ValueExtractor currencyExtractor = new PofExtractor(null, 10);
        Filter currencyFilter = new EqualsFilter(currencyExtractor, "USD");

        ValueExtractor bookExtractor = new PofExtractor(null, 11);
        Filter bookFilter = new EqualsFilter(bookExtractor, "book");

        ValueExtractor traderNameExtractor = new PofExtractor(null, 12);
        Filter traderNameFilter = new EqualsFilter(traderNameExtractor, "traderName");

        Filter allFilters =
            new AllFilter(new Filter[] {currencyFilter, bookFilter, traderNameFilter});

        Set<Integer> keys = state.riskTradeReadCache.keySet(allFilters);
        keys.forEach(key -> state.riskTradeReadCache.get(key));

    }// 64 seconds to apply filter and get 50,000 records out of 100000

    @Benchmark
    public void b06_GetTradeBookFilterHasIndex(InitReadCacheState state) throws Exception {
        ValueExtractor bookExtractor = new PofExtractor(null, 11);
        Filter bookFilter = new EqualsFilter(bookExtractor, "book");

        final AtomicInteger counter = new AtomicInteger(0);

        Set<Integer> result = state.riskTradeReadCache.keySet(bookFilter);

        result.forEach(key ->
                {
                    state.riskTradeReadCache.get(key);
                    counter.incrementAndGet();
                });
        assert (counter.get() > 0);


    } // 129 seconds for getting 100000 records
    //endregion

    private static void persistAllRiskTradesIntoCacheInOneGo(NamedCache<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();

        for (RiskTrade riskTrade : riskTradeList)
        {
            for(int i = 0; i < batchSize; i++) {
                trades.put(riskTrade.getId(), riskTrade);
            }
            riskTradeCache.putAll(trades);
            trades.clear();
        }
        if(trades.size() > 0)
        {
            riskTradeCache.putAll(trades);
            trades.clear();
        }
    }


    private static void fetchAllRecordsOneByOne(NamedCache riskTradeCache) {
        final Set set = riskTradeCache.keySet();
        for (Object key : set) {
            final Object o1 = riskTradeCache.get(key);
        }
    }

    private static void putRiskTrades(NamedCache riskTradeCache, List<RiskTrade> riskTradeList) {
        for (RiskTrade riskTrade : riskTradeList) {
            riskTradeCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private static void putRiskTradesInBulk(NamedCache riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();

        for (RiskTrade riskTrade : riskTradeList)
        {
            for(int i = 0; i < batchSize; i++) {
                trades.put(riskTrade.getId(), riskTrade);
            }
            riskTradeCache.putAll(trades);
            trades.clear();
        }
        if(trades.size() > 0)
        {
            riskTradeCache.putAll(trades);
            trades.clear();
        }
    }

    // local runner for tests
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include("CoherenceUseCasesBenchmark")
            .warmupIterations(0)
            .measurementIterations(1)
            .forks(1)
            .jvmArgs("-ea")
            .shouldFailOnError(false) // switch to "true" to fail the complete run
            .build();

        new Runner(opt).run();
    }
}
