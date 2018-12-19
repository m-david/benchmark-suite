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
    @Measurement(iterations = 100000)
    @Warmup(iterations = 5)
    public void b01_InsertTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        RiskTrade riskTrade = state.riskTradeList.get(state.randomizer.nextInt(state.riskTradeList.size()));
        state.riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);

    }

    @Benchmark
    @Measurement(iterations = 1000)
    @Warmup(iterations = 5)
    public void b02_InsertTradesBulk1000(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = state.randomizer.nextInt(state.riskTradeList.size() - BATCH_SIZE);
        putAllRiskTradesInBulk(
                blackhole,
                state.riskTradeOffHeapCache,
                state.riskTradeList,
                startIndex,
                BATCH_SIZE);
    }

    @Benchmark
    public void b03_GetAllTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        fetchAllRecordsOneByOne(blackhole, state.riskTradeReadCache, state.riskTradeReadCache.keySetOnServer());
    }

    @Benchmark
    public void b04_GetTradeOneFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        Query query = state.clientCache.getQueryService(POOL_NAME).newQuery("select e.id from " + state.riskTradeReadCache.getFullPath() +
                " e where e.settleCurrency = 'USD'");
        SelectResults<Integer> results = (SelectResults) query.execute();
        RiskTrade trade;
        results.forEach(key ->
                {
//                    System.out.println("trade id: " + state.riskTradeReadCache.get(key).getId());
                    state.riskTradeReadCache.get(key);
                });
    }

    @Benchmark
    public void b05_GetTradeThreeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        String queryString = "select e.id from " + state.riskTradeReadCache.getFullPath() +
                " e where e.traderName = 'traderName'" +
                " and e.settleCurrency = 'USD'" +
                " and e.book = 'book'";

//        logger.info(queryString);

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);

        SelectResults<Integer> results = (SelectResults) query.execute();//("traderName", "USD", "book");
//        logger.info(query.getQueryString());
//        logger.info("Results size: " + results.size());

//        System.out.println("**************");
//        System.out.println(queryString);
//        System.out.println("**************");
//        System.out.println("Results size: " + results.size());

        results.forEach(key ->
                {
//                    System.out.println("Trade id: " + state.riskTradeReadCache.get(key));
                    state.riskTradeReadCache.get(key);
                });
    }

    @Benchmark
    public void b06_GetTradeBookFilterHasIndex(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        String queryString =
                "select e.id from " +
                    state.riskTradeReadCache.getFullPath() +
                " e where e.book = 'book'";

        Pool pool = PoolManager.find(POOL_NAME);
        Query query = pool.getQueryService().newQuery(queryString);
        SelectResults<Integer> results = (SelectResults) query.execute();//("book");

//        System.out.println("**************");
//        System.out.println(query.getQueryString());
//        System.out.println("Results size: " + results.size());
//        System.out.println("**************");

//        int expectedCount = riskTradeList.size();
        final AtomicInteger counter = new AtomicInteger(0);

//        logger.info(query.getQueryString());
//        logger.info("Results size: " + results.size());

        results.forEach(key ->
                {
                    state.riskTradeReadCache.get(key);
                    counter.incrementAndGet();
                });
        assert (counter.get() > 0);
    }


}
