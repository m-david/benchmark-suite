package com.ignite.performance;

import common.BenchmarkConstants;
import common.domain.RiskTrade;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.*;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ignite.benchmark.common.IgniteBenchmarkHelper.fetchAllRecordsOneByOne;
import static com.ignite.performance.support.DummyData.getMeDummyRiskTrades;
import static com.ignite.performance.support.DummyData.riskTrade;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
public class IgniteUseCasesBenchmark
{
    private static Logger logger = LoggerFactory.getLogger(IgniteUseCasesBenchmark.class);

    private IgniteClient igniteClient;

    private ClientCache<Integer, RiskTrade> riskTradeMap;

    private ClientCache<Integer, RiskTrade> riskTradeReadMap;

    private ClientCache<Integer, RiskTrade> riskTradeCache;

    // dummy data
    private List<RiskTrade> riskTradeList;

    @Setup
    public void before()
    {
        String addressesString = System.getProperty("benchmark.ignite.addresses", "127.0.0.1:10800");
        ClientConfiguration cfg = new ClientConfiguration().setAddresses(addressesString);

        this.igniteClient = Ignition.startClient(cfg);
        this.riskTradeMap = igniteClient.getOrCreateCache(BenchmarkConstants.TRADE_MAP);
        this.riskTradeReadMap = igniteClient.getOrCreateCache(BenchmarkConstants.TRADE_READ_MAP);

        riskTradeList = getMeDummyRiskTrades();

        populateMap(riskTradeReadMap, riskTradeList);

        this.riskTradeCache = igniteClient.getOrCreateCache(BenchmarkConstants.TRADE_OFFHEAP_MAP);

    }

    @TearDown(Level.Iteration)
    public void afterEach()
    {
        riskTradeMap.clear();
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

    private void persistAllRiskTradesIntoCacheInOneGo(ClientCache<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
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

    private void populateMap(ClientCache<Integer, RiskTrade> workCache, List<RiskTrade> riskTradeList)
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            workCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private Set<Integer> getAllKeys(ClientCache<Integer, RiskTrade> cache)
    {
        Set<Integer> keys = new HashSet<>();
        cache.query(new ScanQuery<>(null)).forEach(entry -> keys.add((Integer) entry.getKey()));
        return keys;
    }

    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        riskTradeList.forEach(trade -> riskTradeMap.put(trade.getId(), trade));
    }

    @Benchmark
    public void b02_InsertTradesBulk() throws Exception
    {
        int batchSize = 500;
        persistAllRiskTradesIntoCacheInOneGo(riskTradeMap, riskTradeList, batchSize);
    }

    @Benchmark
    public void b03_InsertTradesSingleOffHeap() throws Exception {

        for (RiskTrade riskTrade : riskTradeList) {
            riskTradeCache.put(riskTrade.getId(), riskTrade);
        }
    }

    @Benchmark
    public void b04_GetAllRiskTradesSingle() throws Exception
    {
        fetchAllRecordsOneByOne(riskTradeReadMap, getAllKeys(riskTradeReadMap));
    }

    @Benchmark
    public void b05_GetRiskTradeOneFilter() throws Exception
    {
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor =
                riskTradeReadMap.query(
                    new ScanQuery<Integer, RiskTrade>((k, t) -> t.getSettleCurrency().equals("USD"))
                )
             )
        {
            cursor.forEach(entry -> riskTradeReadMap.get(entry.getKey()));
        }
    }

    @Benchmark
    public void b06_GetRiskTradeThreeFilter() throws Exception
    {
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor =
                     riskTradeReadMap.query(
                             new ScanQuery<Integer, RiskTrade>((k, t) ->
                                     t.getSettleCurrency().equals("USD") &&
                                             t.getTraderName().equals("traderName") &&
                                             t.getBook().equals("book")
                             )
                     )
        )
        {
            cursor.forEach(entry -> riskTradeReadMap.get(entry.getKey()));
        }

    }

    private static final String IDX_BOOK = "idx_risk_trade_book";

    @Benchmark
    public void b07_AddIndexOnBookInTradeCacheAndGetDataBookFilter() throws Exception
    {

        SqlFieldsQuery query = new SqlFieldsQuery(
                "CREATE INDEX IF NOT EXISTS " + IDX_BOOK + " ON " + riskTradeReadMap.getName() + " (book)");
        riskTradeReadMap.query(query).getAll();

        SqlQuery sql = new SqlQuery(RiskTrade.class, "book = '?'");

        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = riskTradeReadMap.query(sql.setArgs("book")))
        {
            cursor.forEach(e -> riskTradeReadMap.get(e.getKey()));
        }

    }

    @Benchmark
    public void b08_ContinuousQueryCacheWithBookFilter() throws InterruptedException
    {
        // Creating a continuous query.
        ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();
        SqlQuery sql = new SqlQuery(RiskTrade.class, "book = '?'");
        sql.setArgs("HongkongBook");

        // Setting an optional initial query.
        qry.setInitialQuery(sql);
        // Local listener that is called locally when an update notification is received.
        qry.setLocalListener((evts) ->
                evts.forEach(e -> System.out.println("key=" + e.getKey() + ", val=" + e.getValue())));

        RiskTrade newRiskTradeWithHongKongBook = riskTrade(80000, "HongkongBook");
        RiskTrade newRiskTradeWithSomeOtherBook = riskTrade(80001, "Book");

        // Executing the query.
        try (QueryCursor<Cache.Entry<Integer, String>> cur = riskTradeCache.query(qry))
        {
            riskTradeCache.put(newRiskTradeWithHongKongBook.getId(), newRiskTradeWithHongKongBook);
            riskTradeCache.put(newRiskTradeWithSomeOtherBook.getId(), newRiskTradeWithSomeOtherBook);
        }
    }

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
