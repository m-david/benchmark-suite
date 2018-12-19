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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
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
@OutputTimeUnit(TimeUnit.MICROSECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.SampleTime})
//@Timeout(time = 60, timeUnit = TimeUnit.NANOSECONDS)
public class HazelcastUseCasesBenchmark
{

    private static Logger logger = LoggerFactory.getLogger(HazelcastUseCasesBenchmark.class);

    @State(Scope.Thread)
    public static class InitReadCacheState
    {
        private HazelcastInstance hazelcastClient;
        private IMap<Integer, RiskTrade> riskTradeReadCache;
        private IMap<Integer, RiskTrade> riskTradeOffHeapCache;
        private List<RiskTrade> riskTradeList;

        private ThreadLocalRandom randomizer = ThreadLocalRandom.current();

        @Setup(Level.Trial)
        public void before()
        {
            hazelcastClient = HazelcastClient.newHazelcastClient();
            riskTradeReadCache = hazelcastClient.getMap(TRADE_READ_MAP);
            riskTradeOffHeapCache = hazelcastClient.getMap(TRADE_OFFHEAP_MAP);
            riskTradeList = getMeDummyRiskTrades();

            populateReadMap(riskTradeReadCache, riskTradeList);
        }

        @TearDown(Level.Trial)
        public void afterAll()
        {
            try
            {
                hazelcastClient.shutdown();
            }
            catch (Exception e)
            {
                logger.error(e.getLocalizedMessage());
            }
        }

    }

    @Benchmark
    @Measurement(iterations = 100000)
    @Warmup(iterations = 5)
    public void b01_InsertTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        RiskTrade riskTrade = state.riskTradeList.get(state.randomizer.nextInt(state.riskTradeList.size()));
        state.riskTradeOffHeapCache.set(riskTrade.getId(), riskTrade);

    }

    @Benchmark
    @Measurement(iterations = 1000)
    @Warmup(iterations = 5)
    public void b02_InsertTradesBulk(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = state.randomizer.nextInt(state.riskTradeList.size() - BATCH_SIZE);
        putAllRiskTradesInBulk(blackhole, state.riskTradeOffHeapCache, state.riskTradeList, startIndex, BATCH_SIZE);
    }

    @Benchmark
    public void b03_GetAllTradesSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        fetchAllRecordsOneByOne(blackhole, state.riskTradeReadCache, state.riskTradeReadCache.keySet());
    }

    @Benchmark
    public void b04_GetTradeOneFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        EntryObject e = new PredicateBuilder().getEntryObject();
        Predicate predicate = e.get("settleCurrency").equal("USD");

        Set<Integer> allRiskTradesWhereCurrencyIsUsd = state.riskTradeReadCache.keySet(predicate);
        allRiskTradesWhereCurrencyIsUsd.forEach(key -> state.riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b05_GetTradeThreeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        final EntryObject e = new PredicateBuilder().getEntryObject();
        final Predicate allFilters = Predicates.and(
                e.get("traderName").equal("traderName"),
                e.get("settleCurrency").equal("USD"),
                e.get("book").equal("book")
        );
        Set<Integer> result = state.riskTradeReadCache.keySet(allFilters);
        result.forEach(key -> state.riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b06_GetTradeBookFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        Predicate predicate = new PredicateBuilder().getEntryObject().get("book").equal("book");

        final AtomicInteger counter = new AtomicInteger(0);

        Set<Integer> result = state.riskTradeReadCache.keySet(predicate);
        result.forEach(key ->
            {
                state.riskTradeReadCache.get(key);
                counter.incrementAndGet();
            }
        );

        assert (counter.get() > 0);

    }

    private static void populateReadMap(IMap<Integer, RiskTrade> riskTradeReadIMap, List<RiskTrade> riskTradeList)
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

    private static void putAllRiskTradesInBulk(Blackhole blackhole, Map<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int startIndex, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        for(int i = startIndex; i < batchSize && i < riskTradeList.size(); i++)
        {
            RiskTrade riskTrade = riskTradeList.get(i);
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
    }

    private static void putRiskTrades(IMap<Integer, RiskTrade> riskTradeCache,
                               List<RiskTrade> riskTradeList, boolean useSet)
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            if (!useSet)
            {
                riskTradeCache.put(riskTrade.getId(), riskTrade);
            } else
            {
                riskTradeCache.set(riskTrade.getId(), riskTrade);
            }
        }
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
