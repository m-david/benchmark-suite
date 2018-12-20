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

import static com.geode.benchmark.common.GeodeBenchmarkHelper.fetchAllRecordsOneByOne;
import static com.geode.performance.support.DummyData.getMeDummyRiskTrades;
import static common.BenchmarkConstants.*;

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

    //region FIXTURE
    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b01_InsertTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        RiskTrade riskTrade = state.riskTradeList.get((int) (state.randomizer.nextDouble() * state.riskTradeList.size()));
        state.riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b02_InsertTradesBulk1000(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = ((int) (state.randomizer.nextDouble() * state.riskTradeList.size())) - BATCH_SIZE;
        putAllRiskTradesInBulk(
                blackhole,
                state.riskTradeOffHeapCache,
                state.riskTradeList,
                startIndex,
                BATCH_SIZE);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b03_GetAllTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = (int) (state.randomizer.nextDouble() * state.riskTradeList.size());
        blackhole.consume(state.riskTradeReadCache.get(state.riskTradeList.get(index).getId()));
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b04_GetTradeOneFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = (int) (state.randomizer.nextDouble() * state.riskTradeList.size());
        String currency = DUMMY_CURRENCY+id;

        Query query = state.clientCache.getQueryService(POOL_NAME).newQuery("select * from " + state.riskTradeReadCache.getFullPath() +
                " e where e.settleCurrency = $1");
        SelectResults<RiskTrade> results = (SelectResults) query.execute(currency);

        results.forEach(trade ->
                {
                    assert (trade.getSettleCurrency().equals(currency));
                });
        assert (results.size() > 0);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b05_GetTradeThreeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = (int) (state.randomizer.nextDouble() * state.riskTradeList.size());
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

        results.forEach(trade ->
                {
                    assert (trade.getTraderName().equals(trader) && trade.getSettleCurrency().equals(currency) && trade.getBook().equals(book));
                });
        assert (results.size() > 0);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b06_GetTradeBookFilterHasIndex(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = (int) (state.randomizer.nextDouble() * state.riskTradeList.size());
        String book = DUMMY_BOOK+id;
        String queryString =
                "select * from " +
                    state.riskTradeReadCache.getFullPath() +
                " e where e.book = $1";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);
        SelectResults<RiskTrade> results = (SelectResults) query.execute(book);

        results.forEach(trade ->
                {
                    assert (trade.getBook().equals(book));
                });
        assert (results.size() > 0);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int min = (int) (state.randomizer.nextDouble() * state.riskTradeList.size());
        int max = min + (int) (state.randomizer.nextDouble() * state.riskTradeList.size() * RANGE_PERCENT);
        String queryString =
                "select * from " +
                        state.riskTradeReadCache.getFullPath() +
                        " e where e.id >= $1 and e.id <= $2";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);
        SelectResults<RiskTrade> results = (SelectResults) query.execute(min, max);

        results.forEach(trade ->
        {
            int id = trade.getId();
            assert (id >= min && id <= max);
        });
        assert (results.size() > 0);

    }


}
