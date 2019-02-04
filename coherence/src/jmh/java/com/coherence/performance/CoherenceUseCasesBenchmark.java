package com.coherence.performance;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.PofExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.BetweenFilter;
import com.tangosol.util.filter.EqualsFilter;
import common.Bucket;
import common.BucketImpl;
import common.core.BaseBenchmark;
import common.domain.IRiskTrade;
import common.domain.RiskTrade;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.coherence.poc.serializer.RiskTradeSerializer.*;
import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;
import static common.BenchmarkUtility.getRandomStartIndex;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.SampleTime})
public class CoherenceUseCasesBenchmark extends BaseBenchmark
{

    private static Logger logger = LoggerFactory.getLogger(CoherenceUseCasesBenchmark.class);
    private Bucket<Integer, IRiskTrade> riskTradeReadCache;
    private Bucket<Integer, IRiskTrade> riskTradeOffHeapCache;

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

    private NamedCache<Integer, IRiskTrade> getMap(Bucket<Integer, IRiskTrade> bucket)
    {
        return (NamedCache<Integer, IRiskTrade>) bucket.getMap();
    }

    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include("CoherenceUseCasesBenchmark")
                .warmupIterations(0)
                .measurementIterations(1)
                .forks(1)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();
    }


    @Setup(Level.Trial)
    public void before()
    {
        NamedCache<Integer, IRiskTrade> readCache = CacheFactory.getCache(TRADE_READ_MAP);
        NamedCache<Integer, IRiskTrade> offHeapCache = CacheFactory.getCache(TRADE_OFFHEAP_MAP);

        riskTradeReadCache = new  BucketImpl<Integer, IRiskTrade>(readCache);
        riskTradeOffHeapCache = new BucketImpl<Integer, IRiskTrade>(offHeapCache);

        populateReadCache(NUMBER_OF_TRADES_TO_PROCESS);

        ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
        readCache.addIndex(bookExtractor, false, null);
        ValueExtractor idExtractor = new PofExtractor(null, ID);
        readCache.addIndex(idExtractor, true, null);
        ValueExtractor salesExtractor = new PofExtractor(null, SALES);
        readCache.addIndex(salesExtractor, false, null);

    }

    @TearDown(Level.Trial)
    public void afterAll()
    {
        CacheFactory.shutdown();
    }

    //region fixture
    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        insertSingleTrades(NUMBER_OF_TRADES_TO_PROCESS);
    }

    @Benchmark
    public void b02_InsertTradesBulk(Blackhole blackhole) throws Exception
    {
        insertBulkTrades(NUMBER_OF_TRADES_TO_PROCESS, BATCH_SIZE);
    }

    @Benchmark
    public void b03_GetTradeSingle(Blackhole blackhole) throws Exception
    {
        blackhole.consume(getRandomTrade(NUMBER_OF_TRADES_TO_PROCESS));
    }

    @Benchmark
    public void b04_GetTradeOneFilter() throws Exception
    {
        int id = getRandom();
        String currency = DUMMY_CURRENCY+id;

        ValueExtractor valueExtractor = new PofExtractor(null, SETTLE_CURRENCY);
        Filter filter = new EqualsFilter(valueExtractor, currency);

        Collection<IRiskTrade> results = getMap(riskTradeReadCache).values(filter);
        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
        {
            assert (trade.getSettleCurrency().equals(currency));
            counter.incrementAndGet();
        });
        assert (counter.incrementAndGet() > 0) : String.format("No trades found for settleCurrency: %s", currency);

    }

    @Benchmark
    public void b05_GetTradesThreeFilter() throws Exception
    {
        int id = getRandom();
        String trader = DUMMY_TRADER+id;
        String currency = DUMMY_CURRENCY+id;
        String book = DUMMY_BOOK+id;

        ValueExtractor currencyExtractor = new PofExtractor(null, SETTLE_CURRENCY);
        Filter currencyFilter = new EqualsFilter(currencyExtractor, currency);

        ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
        Filter bookFilter = new EqualsFilter(bookExtractor, book);

        ValueExtractor traderNameExtractor = new PofExtractor(null, TRADER_NAME);
        Filter traderNameFilter = new EqualsFilter(traderNameExtractor, trader);

        Filter allFilters =
                new AllFilter(new Filter[]{currencyFilter, bookFilter, traderNameFilter});

        Collection<IRiskTrade> results = getMap(riskTradeReadCache).values(allFilters);
        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
        {
            assert (trade.getTraderName().equals(trader) && trade.getSettleCurrency().equals(currency) && trade.getBook().equals(book));
            counter.incrementAndGet();
        });
        assert (counter.get() > 0) : String.format("No trades found for traderName: %s, settleCurrency: %s, book: %s", trader, currency, book);

    }

    @Benchmark
    /**
     * Note:  Indexes are only available in Coherence Enterprise Edition and higher
     */
    public void b06_GetTradeIndexedFilter() throws Exception
    {
        int id = getRandom();
        String book = DUMMY_BOOK+id;

        ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
        Filter bookFilter = new EqualsFilter(bookExtractor, book);

        Collection<IRiskTrade> results = getMap(riskTradeReadCache).values(bookFilter);
        AtomicInteger counter = new AtomicInteger(0);
        results.forEach(trade ->
        {
            assert(trade.getBook().equals(book));
            counter.incrementAndGet();
        });
        assert (counter.get() > 0) : String.format("No trades found for book: %s", book);


    }

    @Benchmark
    public void b07_GetTradeIdRangeFilter(Blackhole blackhole) throws Exception
    {
        int range = (int) (NUMBER_OF_TRADES_TO_PROCESS * RANGE_PERCENT);
        int min = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS-range);
        int max = min + range;
        ValueExtractor idExtractor = new PofExtractor(null, ID);

        BetweenFilter betweenFilter = new BetweenFilter(idExtractor, min, max);

        Collection<IRiskTrade> results = getMap(riskTradeReadCache).values(betweenFilter);
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
    public void b08_GetTradeIdRangeAndBookFilter(Blackhole blackhole) throws Exception
    {
        int range = (int) (NUMBER_OF_TRADES_TO_PROCESS * RANGE_PERCENT);
        int min = getRandomStartIndex(NUMBER_OF_TRADES_TO_PROCESS-range);
        int max = min + range;
        int bookId = getRandom(min, max);

        ValueExtractor idExtractor = new PofExtractor(null, ID);
        BetweenFilter betweenFilter = new BetweenFilter(idExtractor, min, max);

        ValueExtractor bookExtractor = new PofExtractor(null, BOOK);
        EqualsFilter bookFilter = new EqualsFilter(bookExtractor, DUMMY_BOOK+bookId);

        AndFilter andFilter = new AndFilter(betweenFilter, bookFilter);

        Collection<IRiskTrade> results = getMap(riskTradeReadCache).values(andFilter);
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
