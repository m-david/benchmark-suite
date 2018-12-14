package com.hazelcast.performance;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.poc.domain.portable.RiskTrade;
import com.hazelcast.query.EntryObject;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.hazelcast.simulator.worker.loadsupport.MapStreamer;
import com.hazelcast.simulator.worker.loadsupport.MapStreamerFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hazelcast.common.BenchmarkHelper.fetchAllRecordsOneByOne;
import static com.hazelcast.performance.support.DummyData.getMeDummyRiskTrades;
import static common.BenchmarkConstants.*;

/**
 * TODO
 *
 * @author Viktor Gamov on 8/12/15.
 * Twitter: @gamussa
 * @since 0.0.1
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.AverageTime})
//@Timeout(time = 60, timeUnit = TimeUnit.SECONDS)
public class HazelcastUseCasesBenchmark
{

    private static Logger logger = LoggerFactory.getLogger(HazelcastUseCasesBenchmark.class);

    private HazelcastInstance hazelcastClient;
    private CacheManager cacheManager;

    // for put benchmarks
//    private IMap<Integer, RiskTrade> riskTradeCache;

    // for read benchmarks
    private IMap<Integer, RiskTrade> riskTradeReadCache;

    // for Offheap JCache Benchmarks
    private IMap<Integer, RiskTrade> riskTradeOffHeapCache;

    // dummy data
    private List<RiskTrade> riskTradeList;

    //region Setup and Tear down
    @Setup
    public void before()
    {
        hazelcastClient = HazelcastClient.newHazelcastClient();

//        riskTradeCache = hazelcastClient.getMap(TRADE_MAP);
        riskTradeReadCache = hazelcastClient.getMap(TRADE_READ_MAP);
        riskTradeOffHeapCache = hazelcastClient.getMap(TRADE_OFFHEAP_MAP);
        riskTradeList = getMeDummyRiskTrades();

        populateReadMap(riskTradeReadCache, riskTradeList);

    }

    @TearDown(Level.Trial)
    public void afterAll()
    {
//        riskTradeReadCache.clear();
        hazelcastClient.shutdown();
    }

    @TearDown(Level.Iteration)
    public void afterEach()
    {
//        riskTradeCache.clear();
//        riskTradeOffHeapCache.clear();
    }
    //endregion

    //region FIXTURE
    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        // puts objects into IMap one by one
//        putRiskTrades(riskTradeCache, riskTradeList, false);
        putRiskTrades(riskTradeOffHeapCache, riskTradeList, true);
    }

    @Benchmark
    public void b02_InsertTradesBulk1000() throws Exception
    {

        putAllRiskTradesInBulk(riskTradeOffHeapCache, riskTradeList, BATCH_SIZE);
    }

//    @Benchmark
//    public void b03_ClearTradesSingle() throws Exception
//    {
//        riskTradeList.forEach(riskTrade -> riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade));
//        riskTradeOffHeapCache.clear();
//    }

    @Benchmark
    public void b03_GetAllTradesSingle() throws Exception
    {
        fetchAllRecordsOneByOne(riskTradeReadCache, riskTradeReadCache.keySet());
    }

    @Benchmark
    public void b04_GetTradeOneFilter() throws Exception
    {
        EntryObject e = new PredicateBuilder().getEntryObject();
        Predicate predicate = e.get("settleCurrency").equal("USD");

        Set<Integer> allRiskTradesWhereCurrencyIsUsd = riskTradeReadCache.keySet(predicate);
        allRiskTradesWhereCurrencyIsUsd.forEach(key -> riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b05_GetTradeThreeFilter() throws Exception
    {
        final EntryObject e = new PredicateBuilder().getEntryObject();
        final Predicate allFilters = Predicates.and(
                e.get("traderName").equal("traderName"),
                e.get("settleCurrency").equal("USD"),
                e.get("book").equal("book")
        );
        Set<Integer> result = riskTradeReadCache.keySet(allFilters);
        result.forEach(key -> riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b06_GetTradeBookFilterHasIndex() throws Exception
    {
        riskTradeReadCache.addIndex("book", false);
        Predicate predicate = new PredicateBuilder().getEntryObject().get("book").equal("book");

        final AtomicInteger counter = new AtomicInteger(0);


        Set<Integer> result = riskTradeReadCache.keySet(predicate);
        result.forEach(key ->
            {
                riskTradeReadCache.get(key);
                counter.incrementAndGet();
            }
        );

        assert (counter.get() > 0);

    }

//    @Benchmark
//    public void b08_ContinuousQueryCacheWithBookFilter() throws InterruptedException
//    {
//
//        Predicate predicate = new PredicateBuilder().getEntryObject().get("book").equal("HongkongBook");
//
//        EntryAddedListener entryAddedListener = new EntryAddedListener()
//        {
//            @Override
//            public void entryAdded(EntryEvent entryEvent)
//            {
//                logger.debug("CQC listener value: " + entryEvent.getValue());
//            }
//        };
//
//        QueryCache<Integer, RiskTrade> onlyTradesBelongToHongKongBookCache =
//                riskTradeReadCache.getQueryCache(TRADE_OFFHEAP_MAP + "cache", entryAddedListener, predicate, true);
//
//        RiskTrade newRiskTradeWithHongKongBook = riskTrade(80000, "HongkongBook");
//        RiskTrade newRiskTradeWithSomeOtherBook = riskTrade(80001, "Book");
//
//        riskTradeOffHeapCache.put(newRiskTradeWithHongKongBook.getId(), newRiskTradeWithHongKongBook);
//        riskTradeOffHeapCache.put(newRiskTradeWithSomeOtherBook.getId(), newRiskTradeWithSomeOtherBook);
//    }
    //endregion


    private void populateReadMap(IMap<Integer, RiskTrade> riskTradeReadIMap, List<RiskTrade> riskTradeList)
    {
        try (MapStreamer<Integer, RiskTrade> mapStreamer = MapStreamerFactory.getInstance(riskTradeReadIMap))
        {
            for (RiskTrade riskTrade : riskTradeList)
            {
                mapStreamer.pushEntry(riskTrade.getId(), riskTrade);
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to close map streamer!", e);
        }
    }

    private void insertRecordsInRiskTradeCacheIfNotExistAlready(IMap<Integer, RiskTrade> riskTradeCache)
    {
        if (riskTradeCache.keySet().size() <= 0)
        {
            putRiskTrades(riskTradeCache, getMeDummyRiskTrades(), false);
        }
    }

    private void putAllRiskTradesInBulk(Map<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
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

    private void putRiskTrades(IMap<Integer, RiskTrade> riskTradeCache,
                               List<RiskTrade> riskTradeList, boolean useSet)
    {
        //MapStreamer mapStreamer = MapStreamerFactory.getInstance(riskTradeCache);
        for (RiskTrade riskTrade : riskTradeList)
        {
            if (!useSet)
            {
                riskTradeCache.put(riskTrade.getId(), riskTrade);
            } else
            {
                riskTradeCache.set(riskTrade.getId(), riskTrade);
            }
            //riskTradeCache.putAsync(riskTrade.getId(), riskTrade);
            //mapStreamer.pushEntry(riskTrade.getId(), riskTrade);
        }
        //mapStreamer.close();
    }


    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(HazelcastUseCasesBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(0)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();
    }
}
