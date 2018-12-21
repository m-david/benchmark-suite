package com.coherence.performance;

import com.coherence.poc.serializer.RiskTradeSerializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.QueryHelper;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.BetweenFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.LikeFilter;
import common.BenchmarkUtility;
import common.domain.RiskTrade;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.coherence.common.CoherenceBenchmarkHelper.getMeDummyRiskTrades;
import static common.BenchmarkConstants.*;
import static com.coherence.poc.serializer.RiskTradeSerializer.*;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.SampleTime})
public class CoherenceUseCasesBenchmark
{

    private static Logger logger = LoggerFactory.getLogger(CoherenceUseCasesBenchmark.class);


    @State(Scope.Thread)
    public static class InitReadCacheState
    {
        private NamedCache<Integer, RiskTrade> riskTradeReadCache;
        private NamedCache<Integer, RiskTrade> riskTradeOffHeapCache;

        private List<RiskTrade> riskTradeList;

        private ThreadLocalRandom randomizer = ThreadLocalRandom.current();

        @Setup(Level.Trial)
        public void before()
        {
            riskTradeReadCache = CacheFactory.getCache(TRADE_READ_MAP);
            riskTradeOffHeapCache = CacheFactory.getCache(TRADE_OFFHEAP_MAP);
            riskTradeList = getMeDummyRiskTrades();
            putRiskTrades(riskTradeReadCache, riskTradeList);

            ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
            riskTradeReadCache.addIndex(bookExtractor, false, null);
            ValueExtractor idExtractor = new PofExtractor(null, ID);
            riskTradeReadCache.addIndex(idExtractor, true, null);
        }

        @TearDown(Level.Trial)
        public void afterAll()
        {
            CacheFactory.shutdown();
        }
    }

    //region FIXTURE
    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b01_InsertTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        RiskTrade riskTrade = state.riskTradeList.get(index);
        state.riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);

    } // 164 seconds for 100000

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b02_InsertTradesBulk(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size() - BATCH_SIZE);
        putAllRiskTradesInBulk(blackhole, state.riskTradeOffHeapCache, state.riskTradeList, startIndex, BATCH_SIZE);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b03_GetTradeSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        blackhole.consume(state.riskTradeReadCache.get(state.riskTradeList.get(index).getId()));
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b04_GetTradeOneFilter(InitReadCacheState state) throws Exception
    {
        int id = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        String currency = DUMMY_CURRENCY+id;

        ValueExtractor valueExtractor = new PofExtractor(null, SETTLE_CURRENCY);
        Filter filter = new EqualsFilter(valueExtractor, currency);

        Collection<RiskTrade> results = state.riskTradeReadCache.values(filter);

        results.forEach(trade ->
        {
            assert (trade.getSettleCurrency().equals(currency));
        });
        assert (results.size() > 0);

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b05_GetTradesThreeFilter(InitReadCacheState state) throws Exception
    {
        int id = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        String trader = DUMMY_TRADER+id;
        String currency = DUMMY_CURRENCY+id;
        String book = DUMMY_BOOK+id;

        ValueExtractor currencyExtractor = new PofExtractor(null, SETTLE_CURRENCY);
        Filter currencyFilter = new EqualsFilter(currencyExtractor, currency);

        ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
        Filter bookFilter = new EqualsFilter(bookExtractor, book);

        ValueExtractor traderNameExtractor = new PofExtractor(null, TRADER_NAME);
        Filter traderNameFilter = new EqualsFilter(traderNameExtractor, trader);

        Filter allFilters =
                new AllFilter(new Filter[]{currencyFilter, bookFilter, traderNameFilter});

        Collection<RiskTrade> results = state.riskTradeReadCache.values(allFilters);
        results.forEach(trade ->
        {
            assert (trade.getTraderName().equals(trader) && trade.getSettleCurrency().equals(currency) && trade.getBook().equals(book));
        });
        assert (results.size() > 0);

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    /**
     * Note:  Indexes are only available in Coherence Enterprise Edition and higher
     */
    public void b06_GetTradeIndexedFilter(InitReadCacheState state) throws Exception
    {
        int id = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        String book = DUMMY_BOOK+id;

        ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
        Filter bookFilter = new EqualsFilter(bookExtractor, book);

        Collection<RiskTrade> results = state.riskTradeReadCache.values(bookFilter);

        results.forEach(trade ->
        {
            assert(trade.getBook().equals(book));
        });
        assert (results.size() > 0);


    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int range = (int) (state.riskTradeList.size() * RANGE_PERCENT);
        int min = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size()-range);
        int max = min + range;
        ValueExtractor idExtractor = new PofExtractor(null, ID);

        BetweenFilter betweenFilter = new BetweenFilter(idExtractor, min, max);

        Collection<RiskTrade> results = state.riskTradeReadCache.values(betweenFilter);

        results.forEach(trade ->
        {
            assert(trade.getId() >= min && trade.getId() <= max);
        });
        assert (results.size() > 0);

    }

        //endregion

    private static void putAllRiskTradesInBulk(Blackhole blackhole, NamedCache<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int startIndex, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        for (int i = startIndex; i < batchSize && i < riskTradeList.size(); i++)
        {
            RiskTrade riskTrade = riskTradeList.get(i);
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
    }


    private static void fetchAllRecordsOneByOne(NamedCache riskTradeCache)
    {
        final Set set = riskTradeCache.keySet();
        for (Object key : set)
        {
            final Object o1 = riskTradeCache.get(key);
        }
    }

    private static void putRiskTrades(NamedCache riskTradeCache, List<RiskTrade> riskTradeList)
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private static void putRiskTradesInBulk(NamedCache riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();

        for (RiskTrade riskTrade : riskTradeList)
        {
            for (int i = 0; i < batchSize; i++)
            {
                trades.put(riskTrade.getId(), riskTrade);
            }
            riskTradeCache.putAll(trades);
            trades.clear();
        }
        if (trades.size() > 0)
        {
            riskTradeCache.putAll(trades);
            trades.clear();
        }
    }

    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
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
