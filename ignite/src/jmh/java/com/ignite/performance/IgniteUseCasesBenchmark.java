package com.ignite.performance;

import com.ignite.poc.model.RiskTrade;
import com.ignite.poc.query.RiskTradeThreeFieldScanQuery;
import common.BenchmarkUtility;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ignite.benchmark.common.DummyData.getMeDummyRiskTrades;
import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;
import static common.BenchmarkUtility.getRandomStartIndex;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.SampleTime})
public class IgniteUseCasesBenchmark
{
    private static Logger logger = LoggerFactory.getLogger(IgniteUseCasesBenchmark.class);

//    private static final String ADDRESSES_PROPERTY_NAME = "benchmark.ignite.discovery.addresses";
//    private static final String PORTS_PROPERTY_NAME = "benchmark.ignite.discovery.ports";
//    private static final String HOSTS_DEFAULT = "127.0.0.1"; // comma separated
//    private static final String PORTS_DEFAULT = "47500..47509";
//    private static final String CLIENT_NAME = "BenchmarkClient-";

    @State(Scope.Thread)
    public static class InitReadCacheState
    {
        private IgniteClient igniteClient;
        private ClientCache<Integer, RiskTrade> riskTradeReadCache;
        private ClientCache<Integer, RiskTrade> riskTradeOffHeapCache;
        private List<RiskTrade> riskTradeList;

        private ThreadLocalRandom randomizer = ThreadLocalRandom.current();

        @Setup(Level.Trial)
        public void before()
        {
            String addressesString = System.getProperty("benchmark.ignite.addresses", "127.0.0.1:40100");
            ClientConfiguration cfg = new ClientConfiguration().setAddresses(addressesString);
            this.igniteClient = Ignition.startClient(cfg);
            this.riskTradeReadCache = igniteClient.cache(TRADE_READ_MAP);
            this.riskTradeOffHeapCache = igniteClient.cache(TRADE_OFFHEAP_MAP);
            this.riskTradeList = getMeDummyRiskTrades();
            populateMap(riskTradeReadCache, riskTradeList);
        }

        @TearDown(Level.Trial)
        public void afterAll()
        {
            try
            {
                igniteClient.close();
            }
            catch (Exception e)
            {
                logger.error(e.getLocalizedMessage());
            }
        }

    }

    private static void putAllRiskTradesInBulk(Blackhole blackhole, ClientCache<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int startIndex, int batchSize)
    {
        Map<Integer, RiskTrade> trades = new HashMap<Integer, RiskTrade>();
        for(int i = startIndex; i < batchSize && i < riskTradeList.size(); i++)
        {
            RiskTrade riskTrade = riskTradeList.get(i);
            trades.put(riskTrade.getId(), riskTrade);
        }
        riskTradeCache.putAll(trades);
    }

    private static void populateMap(ClientCache<Integer, RiskTrade> workCache, List<RiskTrade> riskTradeList)
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            workCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private static Set<Integer> getAllKeys(ClientCache<Integer, RiskTrade> cache)
    {
        Set<Integer> keys = new HashSet<>();
        cache.query(new ScanQuery<>(null)).forEach(entry -> keys.add((Integer) entry.getKey()));
        return keys;
    }

    //region fixture
//    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
//    @Warmup(iterations = 2)
    public void b01_InsertTradeSingle(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int index = getRandomStartIndex(state.riskTradeList.size());
        RiskTrade riskTrade = state.riskTradeList.get(index);
        state.riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
    }

//    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
//    @Warmup(iterations = 2)
    public void b02_InsertTradesBulk(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int startIndex = getRandomStartIndex(state.riskTradeList.size()- BATCH_SIZE);
        putAllRiskTradesInBulk(blackhole, state.riskTradeOffHeapCache, state.riskTradeList, startIndex, BATCH_SIZE);
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
        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "settleCurrency = ?");
        query.setArgs(currency);
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = state.riskTradeReadCache.query(query))
        {
            AtomicInteger counter = new AtomicInteger(0);
            cursor.forEach(entry ->
            {
                assert (entry.getValue().getSettleCurrency().equals(currency));
                counter.incrementAndGet();
            });

            assert (counter.get() > 0) : String.format("No trades found for traderName: %s, settleCurrency: %s, book: %s", currency);
        }
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b05_GetTradesThreeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = getRandomStartIndex(state.riskTradeList.size());
        String trader = DUMMY_TRADER+id;
        String currency = DUMMY_CURRENCY+id;
        String book = DUMMY_BOOK+id;

        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor =
                     state.riskTradeReadCache.query(
                             new ScanQuery<Integer, RiskTrade>(
                         new RiskTradeThreeFieldScanQuery(currency, trader, book))
                         )
                     )
        {
            AtomicInteger counter = new AtomicInteger(0);
            cursor.forEach(entry ->
            {
                entry.getValue().getId();
                RiskTrade trade = entry.getValue();
                assert (
                        trade.getTraderName().equals(trader) &&
                        trade.getSettleCurrency().equals(currency) &&
                        trade.getBook().equals(book)
                    );
                counter.incrementAndGet();
            }
            );
            assert (counter.get() > 0) : String.format("No trades found for traderName: %s, settleCurrency: %s, book: %s", trader, currency, book);
        }

    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b06_GetTradeIndexedFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int id = getRandomStartIndex(state.riskTradeList.size());
        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "book = ?");
        String book = DUMMY_BOOK+id;
        query.setArgs(book);
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = state.riskTradeReadCache.query(query))
        {
            AtomicInteger counter = new AtomicInteger(0);
            cursor.forEach(e ->
                    {
                        assert (e.getValue().getBook().equals(book));
                        counter.incrementAndGet();
                    }
                );
            assert (counter.get() > 0) : String.format("No trades found for book: %s", book);
        }
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int range = (int) (state.riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(state.riskTradeList.size()-range);
        int max = min + range;        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "id >= ? and id <= ?");
        query.setArgs(Integer.valueOf(min), Integer.valueOf(max));
        try(QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = state.riskTradeReadCache.query(query))
        {
            AtomicInteger counter = new AtomicInteger(0);
            blackhole.consume(cursor);
            cursor.forEach(e ->
                    {
                        int id = e.getValue().getId();
                        assert (id >= min && id <= max);
                        counter.incrementAndGet();
                    }
            );
            assert (counter.get() > 0) : String.format("No trades found for id range: %d and %d", min, max);

        }
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b08_GetTradeIdRangeAndBookFilter(Blackhole blackhole, InitReadCacheState state) throws Exception
    {
        int range = (int) (state.riskTradeList.size() * RANGE_PERCENT);
        int min = getRandomStartIndex(state.riskTradeList.size()-range);
        int max = min + range;
        int bookId = getRandom(min, max);
        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "id >= ? and id <= ? and book = ?");

        query.setArgs(Integer.valueOf(min), Integer.valueOf(max), DUMMY_BOOK+bookId);
        try(QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = state.riskTradeReadCache.query(query))
        {
            AtomicInteger counter = new AtomicInteger(0);
            blackhole.consume(cursor);
            cursor.forEach(e ->
                    {
                        int id = e.getValue().getId();
                        assert (id >= min && id <= max);
                        counter.incrementAndGet();
                    }
            );
            assert (counter.get() > 0) : String.format("No trades found for id range: %d and %d", min, max);

        }
    }

    //endregion


    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(IgniteUseCasesBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(0)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();
    }



}
