package common.domain;

import common.Bucket;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static common.BenchmarkConstants.*;

/**
 * TODO
 *
 * @author Viktor Gamov on 8/16/15.
 *         Twitter: @gamussa
 * @since 0.0.1
 */
public class DataHelper {

    public static IRiskTrade createRiskTrade(Supplier<IRiskTrade> tradeSupplier, int id)
    {
            return riskTrade(tradeSupplier, id, DUMMY_BOOK+id, DUMMY_TRADER+id, DUMMY_CURRENCY+id);
    }

    public static IRiskTrade riskTrade(Supplier<IRiskTrade> tradeSupplier, int id, String book, String traderName, String settleCurrency) {
        IRiskTrade riskTrade = tradeSupplier.get();
        riskTrade.setAccrual(20);
        riskTrade.setAction(Action.DUMMY_RISK);
        riskTrade.setBook(book);
        riskTrade.setBuySell(BuySell.BUY);
        riskTrade.setComment("comment");
        riskTrade.setCounterParty("counterParty");
        riskTrade.setEcnLinkId("ecnLinkId");
        riskTrade.setEcnTradeId("ecnTradeId");
        riskTrade.setEnteredDate(new Date());
        riskTrade.setExchange("exchange");
        riskTrade.setFees(new Hashtable());
        riskTrade.setId(id);
        riskTrade.setInventoryId("inventoryId");
        riskTrade.setKeywords(new Hashtable());
        riskTrade.setMarketType("marketType");
        riskTrade.setModifiedUser("modifiedUser");

        riskTrade.setProduct(new RiskBond());
        riskTrade.setProductFamily("productFamily");
        riskTrade.setQuantity(10);
        riskTrade.setSalesPerson("salesPerson");

        riskTrade.setSettleCurrency(settleCurrency);
        riskTrade.setSettleDate(new Date());
        riskTrade.setStatus(TradeStatus.CANCELLED);
        riskTrade.setTradeCurrency("tradeCurrency");
        riskTrade.setTradeDate(new Date());
        riskTrade.setTradePrice(15);
        riskTrade.setTraderName(traderName);
        riskTrade.setTradeSource(TradeSource.BLOOMBERG);
        riskTrade.setTransientKeywords(new Hashtable());
        riskTrade.setUpdatedTime(new Date());
        riskTrade.setEnteredUser("DUMMY");

        return riskTrade;
    }

    public static void putAllRiskTradesInBulk(Bucket<Integer, IRiskTrade> riskTradeCache, Supplier<IRiskTrade> tradeSupplier, int startIndex, int batchSize)
    {
        Map<Integer, IRiskTrade> trades = new HashMap<Integer, IRiskTrade>();
        int limit = Math.min(NUMBER_OF_TRADES_TO_PROCESS, startIndex+batchSize);
        IntStream.range(startIndex, limit).forEach(i ->
        {
            IRiskTrade riskTrade = createRiskTrade(tradeSupplier, i);
            trades.put(riskTrade.getId(), riskTrade);
        });

        riskTradeCache.putAll(trades);

    }

    public static void populateRiskTradeReadCache(Bucket<Integer, IRiskTrade> map, Supplier<IRiskTrade> tradeSupplier, Integer numberOfRecords)
    {
        IntStream.range(1, numberOfRecords).parallel().forEach( id ->
        {
            IRiskTrade riskTrade = createRiskTrade(tradeSupplier, id);
            map.set(riskTrade.getId(), riskTrade);
        });

    }

}
