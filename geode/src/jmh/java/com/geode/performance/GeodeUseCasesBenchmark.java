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
@BenchmarkMode({Mode.SampleTime})
public class GeodeUseCasesBenchmark
{

    private static final String POOL_NAME = "client-pool";

    private static Logger logger = LoggerFactory.getLogger(GeodeUseCasesBenchmark.class);

    public ClientCache clientCache;
    public Region<Integer, RiskTrade> riskTradeReadCache;
    public Region<Integer, RiskTrade> riskTradeOffHeapCache;
    public List<RiskTrade> riskTradeList;

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

    private void putAllRiskTradesInBulk(Region<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int startIndex, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        int limit = Math.min(riskTradeList.size(), startIndex+batchSize);
        for (int i = startIndex; i < limit; i++) {
            RiskTrade riskTrade = riskTradeList.get(i);
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
    }

    //region fixture
    @Benchmark
    public void b01_InsertTradesSingle(Blackhole blackhole) throws Exception
    {
//        AtomicInteger counter = new AtomicInteger(0);
        riskTradeList.forEach(riskTrade ->
        {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
//            if(counter.incrementAndGet() % BATCH_SIZE == 0)
//            {
//                logger.info(String.format("Persisted [%d] records.", counter.get()));
//            }
        });
    }

    @Benchmark
    public void b02_InsertTradesBulk(Blackhole blackhole) throws Exception
    {
        for(int i = 0; i < riskTradeList.size();)
        {
            putAllRiskTradesInBulk(riskTradeOffHeapCache, riskTradeList, i, BATCH_SIZE);
            i = i + BATCH_SIZE;
//            logger.info(String.format("Persisted [%d] records.", i));
        }
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b03_GetTradeSingle(Blackhole blackhole) throws Exception
    {
        int index = getRandomStartIndex(riskTradeList.size());
        blackhole.consume(riskTradeReadCache.get(riskTradeList.get(index).getId()));
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b04_GetTradeOneFilter(Blackhole blackhole) throws Exception
    {
        int id = getRandomStartIndex(riskTradeList.size());
        String currency = DUMMY_CURRENCY+id;

        Query query = clientCache.getQueryService(POOL_NAME).newQuery("select * from " + riskTradeReadCache.getFullPath() +
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
    public void b05_GetTradesThreeFilter(Blackhole blackhole) throws Exception
    {
        int id = getRandomStartIndex(riskTradeList.size());
        String queryString = "select * from " + riskTradeReadCache.getFullPath() +
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
    public void b06_GetTradeIndexedFilter(Blackhole blackhole) throws Exception
    {
        int id = getRandomStartIndex(riskTradeList.size());
        String book = DUMMY_BOOK+id;
        String queryString =
                "select * from " +
                    riskTradeReadCache.getFullPath() +
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
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole) throws Exception
    {
        int range = (int) (riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(riskTradeList.size()-range);
        int max = min + range;
        String queryString =
                "select * from " +
                        riskTradeReadCache.getFullPath() +
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
    public void b08_GetTradeIdRangeAndBookFilter(Blackhole blackhole) throws Exception
    {
        int range = (int) (riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(riskTradeList.size()-range);
        int max = min + range;
        int bookId = getRandom(min, max);
        String queryString =
                "select * from " +
                        riskTradeReadCache.getFullPath() +
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
