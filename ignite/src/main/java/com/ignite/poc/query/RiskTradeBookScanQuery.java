package com.ignite.poc.query;


import com.ignite.poc.model.RiskTrade;
import org.apache.ignite.lang.IgniteBiPredicate;

public class RiskTradeBookScanQuery implements IgniteBiPredicate<Integer, RiskTrade>
{
    private String book;

    public RiskTradeBookScanQuery(String book)
    {
        this.book = book;
    }

    public boolean apply(Integer key, RiskTrade trade)
    {
        return trade.getBook().equals(book);
    }
}
