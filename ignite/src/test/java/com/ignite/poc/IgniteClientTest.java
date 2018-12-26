package com.ignite.poc;

import com.ignite.benchmark.common.DummyData;
import com.ignite.poc.model.RiskTrade;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static common.BenchmarkConstants.TRADE_OFFHEAP_MAP;
import static common.BenchmarkConstants.TRADE_READ_MAP;

public class IgniteClientTest
{

    @Test
    public void testClient()
    {
        String addressesString = System.getProperty("benchmark.ignite.addresses", "127.0.0.1:40100");
        ClientConfiguration cfg = new ClientConfiguration().setAddresses(addressesString);
        IgniteClient igniteClient = Ignition.startClient(cfg);
        Assertions.assertNotNull(igniteClient);


        ClientCache<Integer, RiskTrade> riskTradeReadCache = igniteClient.cache(TRADE_READ_MAP);
        ClientCache<Integer, RiskTrade> riskTradeOffHeapCache = igniteClient.cache(TRADE_OFFHEAP_MAP);

        List<RiskTrade> trades = DummyData.getMeDummyRiskTrades();

        System.out.println("trades size: " + riskTradeOffHeapCache.size());

//        trades.forEach(trade -> riskTradeOffHeapCache.put(trade.getId(), trade));

//        for(int i = 0; i < 5000;i++)
//        {
//            try
//            {
//                Thread.sleep(1000);
//            }
//            catch(InterruptedException e)
//            {
//                e.printStackTrace();
//            }
//
//        }
        try
        {
            igniteClient.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }


    }
}
