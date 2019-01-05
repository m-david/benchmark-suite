package com.hazelcast.performance;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicate;
import com.hazelcast.simulator.worker.loadsupport.MapStreamer;
import com.hazelcast.simulator.worker.loadsupport.MapStreamerFactory;
import common.domain.RiskTrade;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.hazelcast.performance.support.DummyData.getMeDummyRiskTrades;
import static com.hazelcast.query.Predicates.*;
import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;
import static common.BenchmarkUtility.getRandomStartIndex;

@State(Scope.Thread)
public abstract class AbstractHazelcastUseCasesBenchmark {

    private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    abstract Supplier<RiskTrade> tradeSupplier();

    private HazelcastInstance hazelcastClient;
    private IMap<Integer, RiskTrade> riskTradeReadCache;
    private IMap<Integer, RiskTrade> riskTradeOffHeapCache;
    private List<RiskTrade> riskTradeList;

    @Setup(Level.Trial)
    public void before() {
        hazelcastClient = HazelcastClient.newHazelcastClient();
        riskTradeReadCache = hazelcastClient.getMap(TRADE_READ_MAP);
        riskTradeOffHeapCache = hazelcastClient.getMap(TRADE_OFFHEAP_MAP);
        riskTradeList = getMeDummyRiskTrades(tradeSupplier());

        populateReadMap(riskTradeReadCache, riskTradeList);
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
        AtomicInteger counter = new AtomicInteger(0);
        riskTradeList.forEach(riskTrade ->
        {
            riskTradeOffHeapCache.set(riskTrade.getId(), riskTrade);
            if(counter.incrementAndGet() % BATCH_SIZE == 0)
            {
                logger.info(String.format("Persisted [%d] records.", counter.get()));
            }
        });
    }

    @Benchmark
    public void b02_InsertTradesBulk()
    {
        for(int i = 0; i < riskTradeList.size();)
        {
            putAllRiskTradesInBulk(riskTradeOffHeapCache, riskTradeList, i, BATCH_SIZE);
            i = i + BATCH_SIZE;
            logger.info(String.format("Persisted [%d] records.", i));
        }
    }

    @Benchmark
    public void b03_GetTradeSingle(Blackhole blackhole) {
        int index = getRandomStartIndex(riskTradeList.size());
        blackhole.consume(riskTradeReadCache.get(index));
    }

    @Benchmark
    public void b04_GetTradeOneFilter() {
        int id = getRandomStartIndex(riskTradeList.size());
        String currency = DUMMY_CURRENCY + id;

        Collection<RiskTrade> foundTrades = riskTradeReadCache.values(equal("settleCurrency", currency));
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
        int id = getRandomStartIndex(riskTradeList.size());
        String trader = DUMMY_TRADER + id;
        String currency = DUMMY_CURRENCY + id;
        String book = DUMMY_BOOK + id;

        Predicate allFilters = and(
                equal("traderName", trader),
                equal("settleCurrency", currency),
                equal("book", book)
        );
        Collection<RiskTrade> result = riskTradeReadCache.values(allFilters);
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
        int id = getRandomStartIndex(riskTradeList.size());
        String book = DUMMY_BOOK + id;
        Collection<RiskTrade> result = riskTradeReadCache.values(equal("book", book));
        AtomicInteger counter = new AtomicInteger(0);
        result.forEach(trade -> {
            assert (trade.getId() == id);
            counter.incrementAndGet();
        });

        assert (counter.get() > 0) : String.format("No trades found for book: %s", book);
    }

    @Benchmark
    public void b07_GetTradeIdRangeFilter() {
        int range = (int) (riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(riskTradeList.size() - range);
        int max = min + range;
        Collection<RiskTrade> result = riskTradeReadCache.values(between("id", min, max));
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
        int range = (int) (riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(riskTradeList.size() - range);
        int max = min + range;
        String book = DUMMY_BOOK + getRandom(min, max);
        Collection<RiskTrade> result = riskTradeReadCache.values(and(between("id", min, max), equal("book", book)));
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
    //endregion

    private static void populateReadMap(IMap<Integer, RiskTrade> riskTradeReadIMap, List<RiskTrade> riskTradeList) {
        try (MapStreamer<Integer, RiskTrade> mapStreamer = MapStreamerFactory.getInstance(riskTradeReadIMap)) {
            for (RiskTrade riskTrade : riskTradeList) {
                mapStreamer.pushEntry(riskTrade.getId(), riskTrade);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to close map streamer!", e);
        }
    }

    private static void putAllRiskTradesInBulk(Map<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int startIndex, int batchSize) {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        int limit = Math.min(riskTradeList.size(), startIndex+batchSize);
        for (int i = startIndex; i < limit; i++) {
            RiskTrade riskTrade = riskTradeList.get(i);
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
    }

    // local runner for tests

}
