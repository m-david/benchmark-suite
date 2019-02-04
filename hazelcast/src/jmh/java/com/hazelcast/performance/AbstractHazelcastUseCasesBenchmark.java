package com.hazelcast.performance;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import common.Bucket;
import common.BucketImpl;
import common.core.BaseBenchmark;
import common.domain.IRiskTrade;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static com.hazelcast.query.Predicates.*;
import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;
import static common.BenchmarkUtility.getRandomStartIndex;

@State(Scope.Thread)
public abstract class AbstractHazelcastUseCasesBenchmark  extends BaseBenchmark
{

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private HazelcastInstance hazelcastClient;
    private Bucket<Integer, IRiskTrade> riskTradeReadCache;
    private Bucket<Integer, IRiskTrade> riskTradeOffHeapCache;

    @Override
    public Bucket<Integer, IRiskTrade> getWriteBucket() {
        return riskTradeOffHeapCache;
    }

    @Override
    public Bucket<Integer, IRiskTrade> getReadBucket() {
        return riskTradeReadCache;
    }

    @Setup(Level.Trial)
    public void before() {
        hazelcastClient = HazelcastClient.newHazelcastClient();
        riskTradeReadCache = new BucketImpl<>(hazelcastClient.getMap(TRADE_READ_MAP));
        riskTradeOffHeapCache = new BucketImpl<>(hazelcastClient.getMap(TRADE_OFFHEAP_MAP));

        populateReadCache(NUMBER_OF_TRADES_TO_PROCESS);

    }

    @TearDown(Level.Trial)
    public void afterAll() {
        try {
            hazelcastClient.shutdown();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    @Test
    public void runTests() throws RunnerException {
        int warmupCount = this.warmUpCount();
        int runCount = this.runCount();
        Options opts = new OptionsBuilder()
                .include(".*" + this.getClass().getName() + ".b07*")
                .warmupIterations(warmupCount)
                .measurementIterations(runCount)
                .forks(1)
                .build();

        new Runner(opts).run();
    }

    protected int runCount() {
        return 10;
    }

    protected int warmUpCount() {
        return 2;
    }

    //region fixture
    @Benchmark
    public void b01_InsertTradeSingle()
    {
        insertSingleTrades(NUMBER_OF_TRADES_TO_PROCESS);
    }

    @Benchmark
    public void b02_InsertTradesBulk()
    {
        insertBulkTrades(NUMBER_OF_TRADES_TO_PROCESS, BATCH_SIZE);
    }

    @Benchmark
    public void b03_GetTradeSingle(Blackhole blackhole)
    {
        blackhole.consume(getRandomTrade(NUMBER_OF_TRADES_TO_PROCESS));
    }

    @Benchmark
    public void b04_GetTradeOneFilter()
    {
        int id = getRandom();
        String currency = DUMMY_CURRENCY + id;

        Collection<IRiskTrade> foundTrades = getMap(riskTradeReadCache).values(equal("settleCurrency", currency));
        AtomicInteger counter = new AtomicInteger(0);
        foundTrades.forEach(trade ->
        {
            assert (trade.getSettleCurrency().equals(currency));
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : "No trades found for settleCurrency: " + currency;
    }

    @Benchmark
    public void b05_GetTradesThreeFilter() {
        int id = getRandom();
        String trader = DUMMY_TRADER + id;
        String currency = DUMMY_CURRENCY + id;
        String book = DUMMY_BOOK + id;

        Predicate allFilters = and(
                equal("traderName", trader),
                equal("settleCurrency", currency),
                equal("book", book)
        );
        Collection<IRiskTrade> result = getMap(riskTradeReadCache).values(allFilters);
        AtomicInteger counter = new AtomicInteger(0);
        result.forEach(trade -> {
            assert (
                    trade.getTraderName().equals(trader) &&
                            trade.getSettleCurrency().equals(currency) &&
                            trade.getBook().equals(book)
            );
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for traderName: %s, settleCurrency: %s, book: %s", trader, currency, book);

    }

    @Benchmark
    public void b06_GetTradeIndexedFilter() {
        int id = getRandom();
        String book = DUMMY_BOOK + id;
        Collection<IRiskTrade> result = getMap(riskTradeReadCache).values(equal("book", book));
        AtomicInteger counter = new AtomicInteger(0);
        result.forEach(trade -> {
            assert (trade.getId() == id);
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for book: %s", book);
    }

    @Benchmark
    public void b07_GetTradeIdRangeFilter() {
        int range = (int) (NUMBER_OF_TRADES_TO_PROCESS * RANGE_PERCENT);
        int min = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS - range);
        int max = min + range;
        Collection<IRiskTrade> result = getMap(riskTradeReadCache).values(between("id", min, max));
        AtomicInteger counter = new AtomicInteger(0);
        result.forEach(trade ->
        {
            int id = trade.getId();
            assert (id >= min && id <= max);
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for id range: %d and %d", min, max);
    }

    @Benchmark
    public void b08_GetTradeIdRangeAndBookFilter() {
        int range = (int) (NUMBER_OF_TRADES_TO_PROCESS * RANGE_PERCENT);
        int min = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS - range);
        int max = min + range;
        String book = DUMMY_BOOK + getRandom(min, max);
        Collection<IRiskTrade> result = getMap(riskTradeReadCache).values(and(between("id", min, max), equal("book", book)));
        AtomicInteger counter = new AtomicInteger(0);
        result.forEach(trade ->
        {
            int id = trade.getId();
            assert (id >= min && id <= max);
            assert (trade.getBook().equals(book));
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for id range: %d and %d", min, max);
    }

    private IMap<Integer, IRiskTrade> getMap(Bucket<Integer, IRiskTrade> bucket)
    {
        return (IMap<Integer, IRiskTrade>) bucket.getMap();
    }
    //endregion

    // local runner for tests

}
