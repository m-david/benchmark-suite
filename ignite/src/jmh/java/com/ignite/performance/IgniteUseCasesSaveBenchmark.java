package com.ignite.performance;

//@State(Scope.Benchmark)
//@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
public class IgniteUseCasesSaveBenchmark
{
    /**
    private static Logger logger = LoggerFactory.getLogger(IgniteUseCasesSaveBenchmark.class);

    private IgniteClient igniteClient;

    private ClientCache<Integer, RiskTrade> riskTradeCache;

    private ClientCache<Integer, RiskTrade> riskTradeReadCache;

    private ClientCache<Integer, RiskTrade> riskTradeOffHeapCache;

    // dummy data
    private List<RiskTrade> riskTradeList;

    @Setup
    public void before()
    {
        final String addressesString = System.getProperty("benchmark.ignite.addresses", "127.0.0.1:10800");
        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setAddresses(addressesString);

        this.igniteClient = Ignition.startClient(clientConfiguration);

//        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
//
//        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
//
//        // "127.0.0.1:47500..47509"
//        ipFinder.setAddresses(Collections.singletonList(addressesString));
//
//        discoSpi.setIpFinder(ipFinder);
//
//        igniteConfiguration.setDiscoverySpi(discoSpi);
//
//        Ignition.setClientMode(true);
//        this.igniteClient = Ignition.start(igniteConfiguration);


        this.riskTradeCache = igniteClient.getOrCreateCache(BenchmarkConstants.TRADE_MAP);
        this.riskTradeReadCache = igniteClient.getOrCreateCache(BenchmarkConstants.TRADE_READ_MAP);


        riskTradeList = getMeDummyRiskTrades();

        populateMap(riskTradeReadCache, riskTradeList);

        this.riskTradeOffHeapCache = igniteClient.getOrCreateCache(BenchmarkConstants.TRADE_OFFHEAP_MAP);

    }

    @TearDown(Level.Iteration)
    public void afterEach()
    {
        riskTradeCache.clear();
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

//    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        riskTradeList.forEach(trade -> riskTradeCache.put(trade.getId(), trade));
    }

//    @Benchmark
    public void b02_InsertTradesBulk() throws Exception
    {
        int batchSize = 500;
        persistAllRiskTradesIntoCacheInOneGo(riskTradeCache, riskTradeList, batchSize);
    }

//    @Benchmark
    public void b03_InsertTradesSingleOffHeap() throws Exception {

        for (RiskTrade riskTrade : riskTradeList) {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
    }

//    @Benchmark
    public void b03a_ClearTradesSingleOffHeap() throws Exception
    {
        for (RiskTrade riskTrade : riskTradeList)
        {
            riskTradeOffHeapCache.put(riskTrade.getId(), riskTrade);
        }
        riskTradeOffHeapCache.clear();
    }

//    @Benchmark
    public void b04_GetAllRiskTradesSingle() throws Exception
    {
        fetchAllRecordsOneByOne(riskTradeReadCache, getAllKeys(riskTradeReadCache));
    }


//    @Benchmark
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

//    @Benchmark
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
            cursor.forEach(e -> System.out.println(e.getKey()) );
//            cursor.forEach(e -> riskTradeReadCache.get(e.getKey()) );
        }

    }

//    @Benchmark
    public void b08_ContinuousQueryCacheWithBookFilter() throws InterruptedException
    {
        // Creating a continuous query.
        ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();
        SqlQuery sql = new SqlQuery(RiskTrade.class, "book = '?';");
        sql.setArgs("HongkongBook");

        // Setting an optional initial query.
        qry.setInitialQuery(sql);
        // Local listener that is called locally when an update notification is received.
        qry.setLocalListener((evts) ->
                evts.forEach(e -> System.out.println("key=" + e.getKey() + ", val=" + e.getValue())));

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
                .include(IgniteUseCasesSaveBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(0)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();
    }


**/
}
