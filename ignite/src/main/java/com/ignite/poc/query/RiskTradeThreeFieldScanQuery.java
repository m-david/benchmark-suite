package com.ignite.poc.query;

import com.ignite.poc.model.RiskTrade;
import org.apache.ignite.lang.IgniteBiPredicate;

public class RiskTradeThreeFieldScanQuery implements IgniteBiPredicate<Integer, RiskTrade>
{
    private String currency;
    private String traderName;
    private String book;

    public RiskTradeThreeFieldScanQuery(String currency, String traderName, String book)
    {
        this.currency = currency;
        this.traderName = traderName;
        this.book = book;
    }

    public boolean apply(Integer key, RiskTrade trade)
    {
        return trade.getSettleCurrency().equals(currency) &&
                trade.getTraderName().equals(traderName) &&
                trade.getBook().equals(book);

    }
}
