package common;

/**

-Dbenchmark.record.count=100000
-Dbenchmark.batch.size=5000
-Dbenchmark.range.percent=0.05

 **/
public class BenchmarkConstants
{
    public static final int NUMBER_OF_TRADES_TO_PROCESS = Integer.valueOf(System.getProperty("benchmark.record.count", "100000"));
//    public static final int ITERATIONS = Integer.valueOf(System.getProperty("benchmark.iterations", "100000"));

    public static final String TRADE_READ_MAP = "RiskTradeMapRead";
    public static final String TRADE_OFFHEAP_MAP = "RiskTradeMapOffheap";

    public static final int BATCH_SIZE = Integer.valueOf(System.getProperty("benchmark.batch.size", "5000"));;
    public static final double RANGE_PERCENT = Double.valueOf(System.getProperty("benchmark.range.percent", "0.05"));

    public static final String DUMMY_BOOK = "book-";
    public static final String DUMMY_CURRENCY = "USD-";
    public static final String DUMMY_TRADER = "trader-";

}
