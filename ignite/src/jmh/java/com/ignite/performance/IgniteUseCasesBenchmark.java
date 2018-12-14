package com.ignite.performance;

import com.ignite.poc.model.RiskTrade;
import com.ignite.poc.query.RiskTradeBookScanQuery;
import com.ignite.poc.query.RiskTradeSettleCurrencyScanQuery;
import com.ignite.poc.query.RiskTradeThreeFieldScanQuery;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.*;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.ignite.benchmark.common.IgniteBenchmarkHelper.fetchAllRecordsOneByOne;
import static com.ignite.performance.support.DummyData.getMeDummyRiskTrades;
import static com.ignite.performance.support.DummyData.riskTrade;
import static common.BenchmarkConstants.*;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
@BenchmarkMode({Mode.AverageTime})
public class IgniteUseCasesBenchmark
{
    private static Logger logger = LoggerFactory.getLogger(IgniteUseCasesBenchmark.class);

    private IgniteClient igniteClient;

//    private IgniteCache<Integer, RiskTrade> riskTradeCache;

    private ClientCache<Integer, RiskTrade> riskTradeReadCache;

    private ClientCache<Integer, RiskTrade> riskTradeOffHeapCache;

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

    //region Setup and Tear down
    @Setup
    public void before()
    {
//        this.igniteClient = Ignition.start(getConfiguration());
//        igniteClient.active(true);

        String addressesString = System.getProperty("benchmark.ignite.addresses", "127.0.0.1:10800");
        ClientConfiguration cfg = new ClientConfiguration().setAddresses(addressesString);

        this.igniteClient = Ignition.startClient(cfg);


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
//        riskTradeOffHeapCache.clear();
    }

    @TearDown(Level.Trial)
    public void afterAll()
    {
//        riskTradeReadCache.clear();
        try
        {
            igniteClient.close();
        }
        catch (Exception e)
        {
            logger.error(e.getLocalizedMessage());
        }
    }
    //endregion

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

    //region FIXTURE
    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        riskTradeList.forEach(trade -> riskTradeOffHeapCache.put(trade.getId(), trade));
    }

    @Benchmark
    public void b02_InsertTradesBulk1000() throws Exception
    {
        persistAllRiskTradesIntoCacheInOneGo(riskTradeOffHeapCache, riskTradeList, BATCH_SIZE);
    }

//    @Benchmark
//    public void b03_ClearTrades() throws Exception
//    {
//        for (RiskTrade riskTrade : riskTradeList)
//        {
//            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
//        }
//        riskTradeOffHeapCache.clear();
//    }

    @Benchmark
    public void b03_GetAllTradesSingle() throws Exception
    {
        fetchAllRecordsOneByOne(riskTradeReadCache, getAllKeys(riskTradeReadCache));
    }


    @Benchmark
    public void b04_GetTradeSettleCurrencyFilter() throws Exception
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
    public void b05_GetTradeThreeFilter() throws Exception
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

    @Benchmark
    public void b06_GetTradeBookFilterHasIndex() throws Exception
    {
//        SqlQuery sql = new SqlQuery(RiskTrade.class, "book = '?';");

        final AtomicInteger counter = new AtomicInteger(0);

        RiskTradeBookScanQuery query = new RiskTradeBookScanQuery("book");

        try (QueryCursor<Cache.Entry<Integer, RiskTrade>> cursor = riskTradeReadCache.query(new ScanQuery<>(query)))
        {
            cursor.forEach(e ->
                    {
                        riskTradeReadCache.get(e.getKey());
                        counter.incrementAndGet();
                    }
                );
        }

        assert (counter.get() > 0);

    }

//    @Benchmark
//    public void b08_ContinuousQueryCacheWithBookFilter() throws InterruptedException
//    {
//        // Creating a continuous query.
//        ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();
//
//        Query scanQuery = new ScanQuery<>(new RiskTradeBookScanQuery("HongkongBook"));
//
//        // Setting an optional initial query.
//        qry.setInitialQuery(scanQuery);
//        // Local listener that is called locally when an update notification is received.
//        qry.setLocalListener((evts) ->
//                evts.forEach(e -> logger.info("key=" + e.getKey() + ", val=" + e.getValue())));
//
//        RiskTrade newRiskTradeWithHongKongBook = riskTrade(80000, "HongkongBook");
//        RiskTrade newRiskTradeWithSomeOtherBook = riskTrade(80001, "Book");
//
//        // Executing the query.
//        try (QueryCursor<Cache.Entry<Integer, String>> cur = riskTradeOffHeapCache.query(qry))
//        {
//            riskTradeOffHeapCache.put(newRiskTradeWithHongKongBook.getId(), newRiskTradeWithHongKongBook);
//            riskTradeOffHeapCache.put(newRiskTradeWithSomeOtherBook.getId(), newRiskTradeWithSomeOtherBook);
//        }
//    }
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
