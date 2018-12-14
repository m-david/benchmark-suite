package com.coherence.performance;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ContinuousQueryCache;
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

import static com.coherence.common.CoherenceBenchmarkHelper.getMeDummyRiskTrades;
import static com.coherence.common.CoherenceBenchmarkHelper.riskTrade;
import static common.BenchmarkConstants.TRADE_OFFHEAP_MAP;
import static common.BenchmarkConstants.TRADE_READ_MAP;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.AverageTime})
public class CoherenceUseCasesBenchmark {

    private static Logger logger = LoggerFactory.getLogger(CoherenceUseCasesBenchmark.class);

//    private NamedCache<Integer, RiskTrade> riskTradeCache;
    private NamedCache<Integer, RiskTrade> riskTradeReadCache;
    private NamedCache<Integer, RiskTrade> riskTradeOffHeapCache;

    private List<RiskTrade> riskTradeList;

    //region Setup and Tear down
    @Setup
    public void before() {
        //System.setProperty("tangosol.coherence.override", "tangosol-coherence-override.xml");
//        System.setProperty("tangosol.coherence.cacheconfig", "tangosol-java-client-config.xml");
//        System.setProperty("tangosol.pof.config", "my-custom-pof-config.xml");

//        riskTradeCache = CacheFactory.getCache(TRADE_MAP);
        riskTradeReadCache = CacheFactory.getCache(TRADE_READ_MAP);
        riskTradeOffHeapCache = CacheFactory.getCache(TRADE_OFFHEAP_MAP);

        riskTradeList = getMeDummyRiskTrades();

        putRiskTrades(riskTradeReadCache, riskTradeList);

    }

    @TearDown(Level.Iteration)
    public void afterEach() {
//        riskTradeCache.clear();
        riskTradeOffHeapCache.clear();
    }

    @TearDown(Level.Trial)
    public void afterAll()
    {
        riskTradeReadCache.clear();
        CacheFactory.shutdown();
    }
    //endregion

    //region FIXTURE
    @Benchmark
    public void b01_InsertTradesSingle() throws Exception {

        putRiskTrades(riskTradeOffHeapCache, riskTradeList);

    } // 164 seconds for 100000

    @Benchmark
    public void b02_InsertTradesBulk() throws Exception {

        putRiskTradesInBulk(riskTradeOffHeapCache, riskTradeList);

    } // 3 seconds for 100000

    @Benchmark
    public void b03_InsertTradesSingleOffHeap() throws Exception
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
    }

    @Benchmark
    public void b03a_ClearTradesSingleOffHeap() throws Exception
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
        riskTradeOffHeapCache.clear();
    }


    @Benchmark
    public void b04_GetAllRiskTradesSingle() throws Exception {

        fetchAllRecordsOneByOne(riskTradeReadCache);
    } // 131 seconds for 100000

    @Benchmark
    public void b05_GetRiskTradeOneFilter() throws Exception {
        ValueExtractor valueExtractor = new PofExtractor(null, 10);
        Filter filter = new EqualsFilter(valueExtractor, "USD");

        Set allRiskTradesWhereCurrencyIsUsd = riskTradeReadCache.keySet(filter);
        for (Object key : allRiskTradesWhereCurrencyIsUsd) {
            final Object o = riskTradeReadCache.get(key);
        }
    } // 66 seconds to apply filter and get 50,000 records out of 100000

    @Benchmark
    public void b06_GetRiskTradeThreeFilter() throws Exception {

        ValueExtractor currencyExtractor = new PofExtractor(null, 10);
        Filter currencyFilter = new EqualsFilter(currencyExtractor, "USD");

        ValueExtractor bookExtractor = new PofExtractor(null, 11);
        Filter bookFilter = new EqualsFilter(bookExtractor, "book");

        ValueExtractor traderNameExtractor = new PofExtractor(null, 12);
        Filter traderNameFilter = new EqualsFilter(traderNameExtractor, "traderName");

        Filter allFilters =
            new AllFilter(new Filter[] {currencyFilter, bookFilter, traderNameFilter});

        Set result = riskTradeReadCache.keySet(allFilters);
        for (Object key : result) {
            final Object o = riskTradeReadCache.get(key);
        }

    }// 64 seconds to apply filter and get 50,000 records out of 100000

    @Benchmark
    public void b07_AddIndexOnBookInTradeCacheAndGetDataBookFilter() throws Exception {
        ValueExtractor bookExtractor = new PofExtractor(null, 11);
        Filter bookFilter = new EqualsFilter(bookExtractor, "book");
        riskTradeReadCache.addIndex(bookExtractor, false, null);

        Set result = riskTradeReadCache.keySet(bookFilter);
        for (Object key : result) {
            final Object o = riskTradeReadCache.get(key);

        }

    } // 129 seconds for getting 100000 records

//    @Benchmark
    public void b08_ContinuousQueryCacheWithBookFilter() {
        ValueExtractor bookExtractor = new PofExtractor(null, 11);
        Filter bookFilter = new EqualsFilter(bookExtractor, "HongkongBook");
//        riskTradeOffHeapCache.addIndex(bookExtractor, false, null);

        // insertRecordsInRiskTradeCache(riskTradeReadCache);
        ContinuousQueryCache onlyTradesBelongToHongKongBookCache =
            new ContinuousQueryCache(riskTradeReadCache, bookFilter);

        RiskTrade newRiskTradeWithHongKongBook = riskTrade(80000, "HongkongBook");
        RiskTrade newRiskTradeWithSomeOtherBook = riskTrade(80001, "Book");

        riskTradeReadCache.put(newRiskTradeWithHongKongBook.getId(), newRiskTradeWithHongKongBook);
        riskTradeReadCache.put(newRiskTradeWithSomeOtherBook.getId(), newRiskTradeWithSomeOtherBook);
    }
    //endregion

    private void fetchAllRecordsOneByOne(NamedCache riskTradeCache) {
        final Set set = riskTradeCache.keySet();
        for (Object key : set) {
            final Object o1 = riskTradeCache.get(key);
        }
    }

    private void putRiskTrades(NamedCache riskTradeCache, List<RiskTrade> riskTradeList) {
        for (RiskTrade riskTrade : riskTradeList) {
            riskTradeCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private void putRiskTradesInBulk(NamedCache riskTradeCache, List<RiskTrade> riskTradeList) {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        for (RiskTrade riskTrade : riskTradeList) {
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
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
