package com.geode.performance;

import com.geode.domain.serializable.RiskTrade;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.SelectResults;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.geode.performance.support.DummyData.getMeDummyRiskTrades;
import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;
import static common.BenchmarkUtility.getRandomStartIndex;

/**
 * @author mdavid
 * @since
 */

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.SampleTime})
//@Timeout(time = 60, timeUnit = TimeUnit.SECONDS)
public class GeodeUseCasesBenchmark
{

    private static final String POOL_NAME = "client-pool";

    private static Logger logger = LoggerFactory.getLogger(GeodeUseCasesBenchmark.class);

    @State(Scope.Thread)
    public static class InitReadCacheState
    {
        public ClientCache clientCache;
        public Region<Integer, RiskTrade> riskTradeReadCache;
        public Region<Integer, RiskTrade> riskTradeOffHeapCache;
        public List<RiskTrade> riskTradeList;

        private ThreadLocalRandom randomizer = ThreadLocalRandom.current();

        @Setup(Level.Trial)
        public void doSetup()
        {
            ClientCacheFactory ccf = connectStandalone("my-test");
            ccf.setPoolSubscriptionEnabled(true);
            clientCache = ccf.create();
            riskTradeReadCache = clientCache.getRegion(TRADE_READ_MAP);
            riskTradeList = getMeDummyRiskTrades();
            riskTradeOffHeapCache = clientCache.getRegion(TRADE_OFFHEAP_MAP);
            persistAllRiskTradesIntoCache(riskTradeReadCache, riskTradeList);
        }

        @TearDown(Level.Trial)
        public void afterAll()
        {
            clientCache.close();
        }

    }
    private static ClientCacheFactory connectStandalone(String name)
    {
        return new ClientCacheFactory();
    }


    private static void persistAllRiskTradesIntoCache(Region<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList)
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeCache.put(riskTrade.getId(), riskTrade);
        }
    }

    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(GeodeUseCasesBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(0)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();

    }

    private void putAllRiskTradesInBulk(Blackhole blackhole, Region<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int startIndex, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        for(int i = startIndex; i < batchSize && i < riskTradeList.size(); i++)
        {
            RiskTrade riskTrade = riskTradeList.get(i);
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
    }

    //region fixture
//    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b01_InsertTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = getRandomStartIndex(state.riskTradeList.size());
        RiskTrade riskTrade = state.riskTradeList.get(index);
        state.riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);

    }

//    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b02_InsertTradesBulk(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = getRandomStartIndex(state.riskTradeList.size() - BATCH_SIZE);
        putAllRiskTradesInBulk(
                blackhole,
                state.riskTradeOffHeapCache,
                state.riskTradeList,
                startIndex,
                BATCH_SIZE);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b03_GetTradeSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = getRandomStartIndex(state.riskTradeList.size());
        blackhole.consume(state.riskTradeReadCache.get(state.riskTradeList.get(index).getId()));
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b04_GetTradeOneFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = getRandomStartIndex(state.riskTradeList.size());
        String currency = DUMMY_CURRENCY+id;

        Query query = state.clientCache.getQueryService(POOL_NAME).newQuery("select * from " + state.riskTradeReadCache.getFullPath() +
                " e where e.settleCurrency = $1");
        SelectResults<RiskTrade> results = (SelectResults) query.execute(currency);
        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
                {
                    assert (trade.getSettleCurrency().equals(currency));
                    counter.incrementAndGet();
                });
        assert (counter.get() > 0) : String.format("No trades found for settleCurrency: %s", currency);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b05_GetTradesThreeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = getRandomStartIndex(state.riskTradeList.size());
        String queryString = "select * from " + state.riskTradeReadCache.getFullPath() +
                " e where e.traderName = $1" +
                " and e.settleCurrency = $2" +
                " and e.book = $3";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);

        String trader = DUMMY_TRADER+id;
        String currency = DUMMY_CURRENCY+id;
        String book = DUMMY_BOOK+id;

        SelectResults<RiskTrade> results = (SelectResults) query.execute(trader, currency, book);
        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
                {
                    assert (trade.getTraderName().equals(trader) && trade.getSettleCurrency().equals(currency) && trade.getBook().equals(book));
                    counter.incrementAndGet();
                });
        assert (counter.get() > 0) : String.format("No trades found for traderName: %s, settleCurrency: %s, book: %s", trader, currency, book);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b06_GetTradeIndexedFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = getRandomStartIndex(state.riskTradeList.size());
        String book = DUMMY_BOOK+id;
        String queryString =
                "select * from " +
                    state.riskTradeReadCache.getFullPath() +
                " e where e.book = $1";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);
        SelectResults<RiskTrade> results = (SelectResults) query.execute(book);
        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
                {
                    assert (trade.getBook().equals(book));
                    counter.incrementAndGet();
                });
        assert (counter.get() > 0) : String.format("No trades found for book: %s", book);


    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int range = (int) (state.riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(state.riskTradeList.size()-range);
        int max = min + range;
        String queryString =
                "select * from " +
                        state.riskTradeReadCache.getFullPath() +
                        " e where e.id >= $1 and e.id <= $2";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);
        SelectResults<RiskTrade> results = (SelectResults) query.execute(min, max);

        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
        {
            int id = trade.getId();
            assert (id >= min && id <= max);
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for id range: %d and %d", min, max);

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b08_GetTradeIdRangeAndBookFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int range = (int) (state.riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(state.riskTradeList.size()-range);
        int max = min + range;
        int bookId = getRandom(min, max);
        String queryString =
                "select * from " +
                        state.riskTradeReadCache.getFullPath() +
                        " e where e.id >= $1 and e.id <= $2 and e.book = $3";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);
        SelectResults<RiskTrade> results = (SelectResults) query.execute(min, max, DUMMY_BOOK+bookId);

        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
        {
            int id = trade.getId();
            assert (id >= min && id <= max);
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for id range: %d and %d", min, max);

    }

    //endregion


}
