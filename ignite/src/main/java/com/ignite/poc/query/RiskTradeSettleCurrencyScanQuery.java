package com.ignite.poc.query;


import com.ignite.poc.model.RiskTrade;
import org.apache.ignite.lang.IgniteBiPredicate;

public class RiskTradeSettleCurrencyScanQuery implements IgniteBiPredicate<Integer, RiskTrade>
{
    private String currency;

    public RiskTradeSettleCurrencyScanQuery(String currency)
    {
        this.currency = currency;
    }

    public boolean apply(Integer key, RiskTrade trade)
    {
        return trade.getSettleCurrency().equals(currency);
    }
}
