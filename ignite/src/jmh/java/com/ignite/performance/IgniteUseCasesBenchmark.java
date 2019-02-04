package com.ignite.performance;

import com.ignite.benchmark.common.IgniteBucket;
import com.ignite.poc.model.RiskTrade;
import com.ignite.poc.query.RiskTradeThreeFieldScanQuery;
import common.Bucket;
import common.core.BaseBenchmark;
import common.domain.IRiskTrade;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;
import static common.BenchmarkUtility.getRandomStartIndex;
import static common.domain.DataHelper.populateRiskTradeReadCache;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.SampleTime})
public class IgniteUseCasesBenchmark extends BaseBenchmark
{
    private static Logger logger = LoggerFactory.getLogger(IgniteUseCasesBenchmark.class);

    private IgniteClient igniteClient;
    private Bucket<Integer, IRiskTrade> riskTradeReadCache;
    private ClientCache<Integer, IRiskTrade> read;

    private Bucket<Integer, IRiskTrade> riskTradeOffHeapCache;
    private ClientCache<Integer, IRiskTrade> write;

    public Supplier<IRiskTrade> tradeSupplier()
    {
        return () -> new RiskTrade();
    }

    @Override
    public Bucket<Integer, IRiskTrade> getWriteBucket() {
        return riskTradeOffHeapCache;
    }

    @Override
    public Bucket<Integer, IRiskTrade> getReadBucket() {
        return riskTradeReadCache;
    }

    @Setup(Level.Trial)
    public void before()
    {
        String addressesString = System.getProperty("benchmark.ignite.addresses", "127.0.0.1:40100");
        ClientConfiguration cfg = new ClientConfiguration().setAddresses(addressesString);
        this.igniteClient = Ignition.startClient(cfg);
        this.read = igniteClient.cache(TRADE_READ_MAP);
        this.write = igniteClient.cache(TRADE_OFFHEAP_MAP);
        this.riskTradeReadCache = new IgniteBucket<>(read);
        this.riskTradeOffHeapCache = new IgniteBucket<>(write);
        populateRiskTradeReadCache(riskTradeReadCache, tradeSupplier(), NUMBER_OF_TRADES_TO_PROCESS);
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

    //region fixture
    @Benchmark
    public void b01_InsertTradeSingle() throws Exception
    {
        insertSingleTrades(NUMBER_OF_TRADES_TO_PROCESS);
    }

    @Benchmark
    public void b02_InsertTradesBulk() throws Exception
    {
        insertBulkTrades(NUMBER_OF_TRADES_TO_PROCESS, BATCH_SIZE);

    }

    @Benchmark
    public void b03_GetTradeSingle(Blackhole blackhole) throws Exception
    {
        blackhole.consume(getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS));
    }


    @Benchmark
    public void b04_GetTradeOneFilter(Blackhole blackhole) throws Exception
    {
        int id = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS);
        String currency = DUMMY_CURRENCY+id;
        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "settleCurrency = ?");
        query.setArgs(currency);
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = read.query(query))
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
    public void b05_GetTradesThreeFilter(Blackhole blackhole) throws Exception
    {
        int id = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS);
        String trader = DUMMY_TRADER+id;
        String currency = DUMMY_CURRENCY+id;
        String book = DUMMY_BOOK+id;

        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor =
                     read.query(
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
    public void b06_GetTradeIndexedFilter(Blackhole blackhole) throws Exception
    {
        int id = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS);
        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "book = ?");
        String book = DUMMY_BOOK+id;
        query.setArgs(book);
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = read.query(query))
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
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole) throws Exception
    {
        int range = (int) (NUMBER_OF_TRADES_TO_PROCESS * RANGE_PERCENT);
        int min = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS-range);
        int max = min + range;        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "id >= ? and id <= ?");
        query.setArgs(Integer.valueOf(min), Integer.valueOf(max));
        try(QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = read.query(query))
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
    public void b08_GetTradeIdRangeAndBookFilter(Blackhole blackhole) throws Exception
    {
        int range = (int) (NUMBER_OF_TRADES_TO_PROCESS * RANGE_PERCENT);
        int min = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS-range);
        int max = min + range;
        int bookId = getRandom(min, max);
        SqlQuery<Integer, RiskTrade> query = new SqlQuery<>(RiskTrade.class, "id >= ? and id <= ? and book = ?");

        query.setArgs(Integer.valueOf(min), Integer.valueOf(max), DUMMY_BOOK+bookId);
        try(QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = read.query(query))
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
