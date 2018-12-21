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
import common.BenchmarkUtility;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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
    @Measurement(iterations = ITERATIONS)
    @Warmup(iterations = 2)
    public void b01_InsertTradeSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        RiskTrade riskTrade = state.riskTradeList.get(index);
        state.riskTradeOffHeapCache.set(riskTrade.getId(), riskTrade);

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    @Warmup(iterations = 2)
    public void b02_InsertTradesBulk(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size()-BATCH_SIZE);
        putAllRiskTradesInBulk(blackhole, state.riskTradeOffHeapCache, state.riskTradeList, startIndex, BATCH_SIZE);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
//    @Warmup(iterations = 5)
    public void b03_GetTradeSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        blackhole.consume(state.riskTradeReadCache.get(index));
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b04_GetTradeOneFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        String trader = DUMMY_TRADER+id;
        Predicate predicate = new PredicateBuilder().getEntryObject().get("traderName").equal(trader);

        Collection<RiskTrade> foundTrades = state.riskTradeReadCache.values(predicate);
        foundTrades.forEach(trade ->
        {
            assert (trade.getTraderName().equals(trader));
        });

        assert (foundTrades.size() > 0);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b05_GetTradesThreeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        String trader = DUMMY_TRADER+id;
        String currency = DUMMY_CURRENCY+id;
        String book = DUMMY_BOOK+id;

        EntryObject e = new PredicateBuilder().getEntryObject();
        Predicate allFilters = Predicates.and(
                e.get("traderName").equal(trader),
                e.get("settleCurrency").equal(currency),
                e.get("book").equal(book)
        );
        Collection<RiskTrade> result = state.riskTradeReadCache.values(allFilters);
        result.forEach(trade -> {
            assert (
                    trade.getTraderName().equals(trader) &&
                    trade.getSettleCurrency().equals(currency) &&
                    trade.getBook().equals(book)
            );
        });

        assert(result.size() > 0);

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b06_GetTradeIndexedFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size());
        String book = DUMMY_BOOK+id;
        Predicate predicate = new PredicateBuilder().getEntryObject().get("book").equal(book);
        Collection<RiskTrade> result = state.riskTradeReadCache.values(predicate);
        result.forEach(trade -> {
            assert (trade.getId() == id);
        });

        assert(result.size() > 0);
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS)
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int range = (int) (state.riskTradeList.size() * RANGE_PERCENT);
        int min = BenchmarkUtility.getRandomStartIndex(state.riskTradeList.size()-range);
        int max = min + range;
        Collection<RiskTrade> result = state.riskTradeReadCache.values(Predicates.between("id", min, max));
        result.forEach(trade -> {
            assert (trade.getId() >= min && trade.getId() <= max);
        });

        assert(result.size() > 0);
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
