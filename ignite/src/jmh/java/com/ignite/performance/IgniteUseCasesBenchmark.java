package com.ignite.performance;

import com.ignite.poc.model.RiskTrade;
import com.ignite.poc.query.RiskTradeBookScanQuery;
import com.ignite.poc.query.RiskTradeSettleCurrencyScanQuery;
import com.ignite.poc.query.RiskTradeThreeFieldScanQuery;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.*;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
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
import static common.BenchmarkConstants.*;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
public class IgniteUseCasesBenchmark
{
    private static Logger logger = LoggerFactory.getLogger(IgniteUseCasesBenchmark.class);

    private Ignite igniteClient;

//    private IgniteCache<Integer, RiskTrade> riskTradeCache;

    private IgniteCache<Integer, RiskTrade> riskTradeReadCache;

    private IgniteCache<Integer, RiskTrade> riskTradeOffHeapCache;

    private static final String ADDRESSES_PROPERTY_NAME = "benchmark.ignite.discovery.addresses";
    private static final String PORTS_PROPERTY_NAME = "benchmark.ignite.discovery.ports";

    private static final String HOSTS_DEFAULT = "127.0.0.1"; // comma separated
    private static final String PORTS_DEFAULT = "47500..47509";

    private static final String CLIENT_NAME = "BenchmarkClient-";

    // dummy data
    private List<RiskTrade> riskTradeList;

    private IgniteConfiguration getConfiguration()
    {
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setIgniteInstanceName(CLIENT_NAME + Thread.currentThread().getName());
        igniteConfiguration.setClientMode(true);
        igniteConfiguration.setDiscoverySpi(getDiscoverySpi());
        return igniteConfiguration;
    }

    private DiscoverySpi getDiscoverySpi()
    {
        String addressesString = System.getProperty(ADDRESSES_PROPERTY_NAME, HOSTS_DEFAULT);
        String portsString = System.getProperty(PORTS_PROPERTY_NAME, PORTS_DEFAULT);
        String[] addresses = addressesString.split(",");

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        List<String> list = new ArrayList<>();
        List<String> ultimateAddressList = new ArrayList<>();
        Collections.addAll(list, addresses);
        list.forEach(s -> ultimateAddressList.add(s + ":" + portsString));
        ipFinder.setAddresses(ultimateAddressList);
        discoSpi.setIpFinder(ipFinder);
        return discoSpi;
    }

    @Setup
    public void before()
    {
        this.igniteClient = Ignition.start(getConfiguration());
        igniteClient.active(true);
//        this.riskTradeCache = igniteClient.cache(TRADE_MAP);
        this.riskTradeReadCache = igniteClient.cache(TRADE_READ_MAP);
        this.riskTradeOffHeapCache = igniteClient.cache(TRADE_OFFHEAP_MAP);

        this.riskTradeList = getMeDummyRiskTrades();
        populateMap(riskTradeReadCache, riskTradeList);
    }

    @TearDown(Level.Iteration)
    public void afterEach()
    {
//        riskTradeCache.clear();
        riskTradeOffHeapCache.clear();
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

    private void persistAllRiskTradesIntoCacheInOneGo(IgniteCache<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
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

    private void populateMap(IgniteCache<Integer, RiskTrade> workCache, List<RiskTrade> riskTradeList)
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            workCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private Set<Integer> getAllKeys(IgniteCache<Integer, RiskTrade> cache)
    {
        Set<Integer> keys = new HashSet<>();
        cache.query(new ScanQuery<>(null)).forEach(entry -> keys.add((Integer) entry.getKey()));
        return keys;
    }

    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        riskTradeList.forEach(trade -> riskTradeOffHeapCache.put(trade.getId(), trade));
    }

    @Benchmark
    public void b02_InsertTradesBulk() throws Exception
    {
        int batchSize = 500;
        persistAllRiskTradesIntoCacheInOneGo(riskTradeOffHeapCache, riskTradeList, batchSize);
    }

    @Benchmark
    public void b03_InsertTradesSingleOffHeap() throws Exception {

        for (RiskTrade riskTrade : riskTradeList) {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
    }

    @Benchmark
    public void b03a_ClearTradesSingleOffHeap() throws Exception
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
        riskTradeOffHeapCache.clear();
    }

    @Benchmark
    public void b04_GetAllRiskTradesSingle() throws Exception
    {
        fetchAllRecordsOneByOne(riskTradeReadCache, getAllKeys(riskTradeReadCache));
    }


    @Benchmark
    public void b05_GetRiskTradeOneFilter() throws Exception
    {

        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor =
                riskTradeReadCache.query(
                    new ScanQuery<Integer, RiskTrade>(new RiskTradeSettleCurrencyScanQuery("USD"))
                )
             )
        {
            cursor.forEach(entry -> riskTradeReadCache.get(entry.getKey()));
        }
    }

    @Benchmark
    public void b06_GetRiskTradeThreeFilter() throws Exception
    {
        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor =
                     riskTradeReadCache.query(
                             new ScanQuery<Integer, RiskTrade>(
                         new RiskTradeThreeFieldScanQuery("USD", "traderName", "book"))
                         )
                     )
        {
            cursor.forEach(entry -> riskTradeReadCache.get(entry.getKey()));
        }

    }

    private static final String IDX_BOOK = "risk_trade_book_idx";

    @Benchmark
    public void b07_AddIndexOnBookInTradeCacheAndGetDataBookFilter() throws Exception
    {

//        SqlFieldsQuery query = new SqlFieldsQuery(
//                "CREATE INDEX IF NOT EXISTS " + IDX_BOOK + " ON " + BenchmarkConstants.TRADE_READ_MAP + " (book);");
//        riskTradeReadCache.query(query).getAll();

        SqlQuery sql = new SqlQuery(RiskTrade.class, "book = '?';");

        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = riskTradeReadCache.query(sql.setArgs("book")))
        {
//            cursor.forEach(e -> System.out.println(e.getKey()) );
            cursor.forEach(e -> riskTradeReadCache.get(e.getKey()) );
        }

    }

    @Benchmark
    public void b08_ContinuousQueryCacheWithBookFilter() throws InterruptedException
    {
        // Creating a continuous query.
        ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();

        Query scanQuery = new ScanQuery<>(new RiskTradeBookScanQuery("HongkongBook"));

        // Setting an optional initial query.
        qry.setInitialQuery(scanQuery);
        // Local listener that is called locally when an update notification is received.
        qry.setLocalListener((evts) ->
                evts.forEach(e -> logger.info("key=" + e.getKey() + ", val=" + e.getValue())));

        RiskTrade newRiskTradeWithHongKongBook = riskTrade(80000, "HongkongBook");
        RiskTrade newRiskTradeWithSomeOtherBook = riskTrade(80001, "Book");

        // Executing the query.
        try (QueryCursor<Cache.Entry<Integer, String>> cur = riskTradeOffHeapCache.query(qry))
        {
            riskTradeOffHeapCache.put(newRiskTradeWithHongKongBook.getId(), newRiskTradeWithHongKongBook);
            riskTradeOffHeapCache.put(newRiskTradeWithSomeOtherBook.getId(), newRiskTradeWithSomeOtherBook);
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
