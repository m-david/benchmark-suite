package com.coherence.common;

import common.domain.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import static common.BenchmarkConstants.*;

/**
 * TODO
 *
 * @author Viktor Gamov on 1/8/16.
 *         Twitter: @gamussa
 * @since 0.0.1
 */
public class CoherenceBenchmarkHelper {
        public static RiskTrade riskTrade(int id, String book, String traderName, String settleCurrency, String salesPerson) {
            RiskTrade riskTrade = new RiskTrade();
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
            // TODO
            //riskTradeCache.setProduct(new RiskBond());
            riskTrade.setProductFamily("productFamily");
            riskTrade.setQuantity(10);
            riskTrade.setSalesPerson(salesPerson);
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
            return riskTrade;
        }

    public static List<RiskTrade> getMeDummyRiskTrades() {
        List<RiskTrade> riskTrades = new ArrayList<RiskTrade>();
        for (int i = 0; i < NUMBER_OF_TRADES_TO_PROCESS; i++) {
            riskTrades.add(riskTrade(i, DUMMY_BOOK+i, DUMMY_TRADER+i, DUMMY_CURRENCY+i, DUMMY_SALES+(i%CARDINALITY)));
        }
        return riskTrades;
    }
}
