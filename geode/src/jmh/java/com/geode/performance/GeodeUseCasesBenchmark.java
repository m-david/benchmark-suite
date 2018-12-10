package com.geode.performance;

import com.geode.domain.serializable.RiskTrade;
import com.geode.benchmark.common.GeodeBenchmarkHelper;
import com.geode.benchmark.event.RiskTradeCqListener;
import com.geode.benchmark.function.CreateIndexFunction;
import com.geode.benchmark.function.PartitionRegionClearFunction;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.Pool;
import org.apache.geode.cache.client.PoolManager;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.query.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.geode.performance.support.DummyData.getMeDummyRiskTrades;
import static com.geode.performance.support.DummyData.riskTrade;
import static com.geode.benchmark.common.GeodeBenchmarkHelper.fetchAllRecordsOneByOne;
import static common.BenchmarkConstants.TRADE_MAP;
import static common.BenchmarkConstants.TRADE_OFFHEAP_MAP;
import static common.BenchmarkConstants.TRADE_READ_MAP;

/**
 * @author mdavid
 * @since
 */

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
//@Timeout(time = 60, timeUnit = TimeUnit.SECONDS)
public class GeodeUseCasesBenchmark
{

    private static final String POOL_NAME = "client-pool";

    private static Logger logger = LoggerFactory.getLogger(GeodeUseCasesBenchmark.class);

    private ClientCache clientCache;

    // for put benchmarks
    private Region<Integer, RiskTrade> riskTradeCache;

    // for read benchmarks
    private Region<Integer, RiskTrade> riskTradeReadCache;

    // for Offheap Region Benchmarks
    private Region<Integer, RiskTrade> riskTradeOffHeapCache;

    // dummy data
    private List<RiskTrade> riskTradeList;


    //region FIXTURE
    @Setup
    public void before()
    {
        ClientCacheFactory ccf = connectStandalone("my-test");
        ccf.setPoolSubscriptionEnabled(true);
        clientCache = ccf.create();

        FunctionService.registerFunction(new PartitionRegionClearFunction());

        riskTradeCache = clientCache.getRegion(TRADE_MAP);
        riskTradeReadCache = clientCache.getRegion(TRADE_READ_MAP);
        riskTradeOffHeapCache = clientCache.getRegion(TRADE_OFFHEAP_MAP);

        riskTradeList = getMeDummyRiskTrades();

        persistAllRiskTradesIntoCache(riskTradeReadCache, riskTradeList);

    }

    @TearDown(Level.Trial)
    public void afterAll()
    {
        clientCache.close();
    }

    @TearDown(Level.Iteration)
    public void afterEach()
    {
        clearRegion(riskTradeCache);
        clearRegion(riskTradeOffHeapCache);
    }

    private void clearRegion(Region<Integer, RiskTrade> region)
    {
        if(riskTradeCache.size() < 1)
        {
            logger.info("clearRegion ... nothing to do.");
            return;
        }

        logger.info(">> before clear, riskTradeCache.size: " + region.keySetOnServer().size() + " fullPath: " + region.getFullPath());
        GeodeBenchmarkHelper.removeBatch(region, region.keySetOnServer(), 1000);
        logger.info(">> after clear, riskTradeCache.size: " + region.keySetOnServer().size());
    }

    private void persistAllRiskTradesIntoCache(Region<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList)
    {
        if(riskTradeCache.keySetOnServer().size() == riskTradeList.size())
        {
            return;
        }
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeCache.put(riskTrade.getId(), riskTrade);
        }
    }

    private ClientCacheFactory connectStandalone(String name)
    {
        return new ClientCacheFactory()
//                .set("log-file", "./logs/" + name + ".log")
//                .set("statistic-archive-file", "./logs/" + name + ".gfs")
//                .set("statistic-sampling-enabled", "true")
//                .addPoolLocator("localhost", Integer.valueOf(System.getProperty("LOCATOR_PORT", "10680")))
                ;
    }


    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(GeodeUseCasesBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(0)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();

    }

    private void persistAllRiskTradesIntoCacheInOneGo(Region<Integer, RiskTrade> riskTradeCache, List<RiskTrade> riskTradeList, int batchSize)
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

    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        persistAllRiskTradesIntoCache(riskTradeCache, riskTradeList);

        assert riskTradeCache.keySetOnServer().size() == riskTradeList.size();
    }

    @Benchmark
    public void b02_InsertTradesBulk() throws Exception
    {
        int batchSize = 500;
        persistAllRiskTradesIntoCacheInOneGo(riskTradeCache, riskTradeList, batchSize);
    }

    @Benchmark
    public void b03_InsertTradesSingleOffHeap() throws Exception {

        for (RiskTrade riskTrade : riskTradeList) {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
    }

    @Benchmark
    public void b04_GetAllRiskTradesSingle() throws Exception
    {
        fetchAllRecordsOneByOne(riskTradeReadCache, riskTradeReadCache.keySetOnServer());
    }

    @Benchmark
    public void b05_GetRiskTradeOneFilter() throws Exception
    {
        Query query = clientCache.getQueryService(POOL_NAME).newQuery("select e.id from " + riskTradeReadCache.getFullPath() +
                " e where e.settleCurrency = '$1'");
        SelectResults<Integer> results = (SelectResults) query.execute("USD");
        results.forEach(key -> riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b06_GetRiskTradeThreeFilter() throws Exception
    {
        String queryString = "select e.id from " + riskTradeReadCache.getFullPath() +
                " e where e.traderName = '$1'" +
                " and e.settleCurrency = '$2'" +
                " and e.book = '$3'";

        logger.info(queryString);
        Query query = clientCache.getQueryService(POOL_NAME).newQuery(queryString);

        SelectResults<Integer> results = (SelectResults) query.execute("traderName", "USD", "book");
        logger.info(query.getQueryString());
        logger.info("Results size: " + results.size());
        results.forEach(key -> riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b07_AddIndexOnBookInTradeCacheAndGetDataBookFilter() throws Exception
    {
        Pool pool = PoolManager.find(POOL_NAME);
        assert pool != null;

        FunctionService.onServer(pool).setArguments(new Object[]
                {
                        riskTradeReadCache.getName() +".bookIndex", "book", riskTradeReadCache.getFullPath()
                }).execute(CreateIndexFunction.ID);

        Query query = clientCache.getQueryService(POOL_NAME).newQuery("select e.id from " + riskTradeReadCache.getFullPath() + " e where e.book = '$1'");
        SelectResults<Integer> results = (SelectResults) query.execute("book");

        logger.info(query.getQueryString());
        logger.info("Results size: " + results.size());
        results.forEach(key -> riskTradeReadCache.get(key));
    }

    @Benchmark
    public void b08_ContinuousQueryCacheWithBookFilter() throws InterruptedException {

        // Create CqAttribute using CqAttributeFactory
        CqAttributesFactory cqf = new CqAttributesFactory();

        // Create a listener and add it to the CQ attributes callback defined below
        CqListener tradeEventListener = new RiskTradeCqListener();
        cqf.addCqListener(tradeEventListener);
        CqAttributes cqa = cqf.create();
        // Name of the CQ and its query
        String cqName = "riskTradeTracker";
        String queryString = "select * from " + riskTradeOffHeapCache.getFullPath() + " t where t.book = 'HongkongBook'";

        CqQuery cqQuery = null;
        try
        {
            cqQuery = clientCache.getQueryService(POOL_NAME).getCq(cqName);
            if(cqQuery == null)
            {
                cqQuery = clientCache.getQueryService().newCq(cqName, queryString, cqa);
            }
        } catch (CqExistsException e)
        {
            e.printStackTrace();
        } catch (CqException e)
        {
            e.printStackTrace();
            return;
        }

        RiskTrade newRiskTradeWithHongKongBook = riskTrade(80000, "HongkongBook");
        RiskTrade newRiskTradeWithSomeOtherBook = riskTrade(80001, "Book");

        riskTradeOffHeapCache.put(newRiskTradeWithHongKongBook.getId(), newRiskTradeWithHongKongBook);
        riskTradeOffHeapCache.put(newRiskTradeWithSomeOtherBook.getId(), newRiskTradeWithSomeOtherBook);
    }


}
