package com.benchmark.test;

import common.BenchmarkUtility;
import common.core.BaseBenchmark;
import common.domain.DataHelper;
import common.domain.IRiskTrade;
import common.domain.RiskTrade;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static common.domain.DataHelper.createRiskTrade;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SupplierTest {

    Supplier<IRiskTrade> tradeSupplier()
    {
        return () -> new RiskTrade();
    }

    @Test
    public void testCreation()
    {
        IRiskTrade riskTrade = DataHelper.createRiskTrade(tradeSupplier(), 0);
        assertEquals(Integer.valueOf(0), riskTrade.getId());
        assertEquals("book-0", riskTrade.getBook());
        assertEquals("trader-0", riskTrade.getTraderName());
        assertEquals("USD-0", riskTrade.getSettleCurrency());

        IRiskTrade riskTrade2 = DataHelper.createRiskTrade(tradeSupplier(), 1000);
        assertEquals(Integer.valueOf(1000), riskTrade2.getId());
        assertEquals("book-1000", riskTrade2.getBook());
        assertEquals("trader-1000", riskTrade2.getTraderName());
        assertEquals("USD-1000", riskTrade2.getSettleCurrency());
    }

    @Test
    public void testIntStream()
    {
        int maxNumber = 100;
        Map<Integer, IRiskTrade> map = new HashMap<>();
        IntStream.range(0, maxNumber).forEach(id ->
        {
            IRiskTrade riskTrade = createRiskTrade(tradeSupplier(), id);
            map.put(riskTrade.getId(), riskTrade);
        });

        assertEquals(maxNumber, map.values().size());

        IntStream.range(0, maxNumber).forEach(id ->
        {
            IRiskTrade riskTrade = map.get(id);
            assertEquals("book-" + id, map.get(id).getBook());
            assertEquals("trader-" + id, map.get(id).getTraderName());
            assertEquals("USD-" + id, map.get(id).getSettleCurrency());
        }
        );
    }
}
