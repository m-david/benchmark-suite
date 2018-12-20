package common;

/**
 * TODO
 *
 * @author Viktor Gamov on 12/7/15.
 *         Twitter: @gamussa
 * @since 0.0.1
 */
public class BenchmarkConstants {
    public static final int NUMBER_OF_TRADES_TO_PROCESS = 100000;
    public static final int ITERATIONS = NUMBER_OF_TRADES_TO_PROCESS;

//    public static final String TRADE_MAP = "RiskTradeMap";
    public static final String TRADE_READ_MAP = "RiskTradeMapRead";
    public static final String TRADE_OFFHEAP_MAP = "RiskTradeMapOffheap";

    public static final int BATCH_SIZE = 5000;
    public static final double RANGE_PERCENT = 0.05;

    public static final String DUMMY_BOOK = "book-";
    public static final String DUMMY_CURRENCY = "USD-";
    public static final String DUMMY_TRADER = "trader-";

}
