package common.core;

import common.Bucket;
import common.domain.IRiskTrade;
import common.domain.RiskTrade;

import java.util.function.Supplier;
import java.util.stream.IntStream;

import static common.BenchmarkConstants.NUMBER_OF_TRADES_TO_PROCESS;
import static common.BenchmarkUtility.getRandomStartIndex;
import static common.domain.DataHelper.createRiskTrade;
import static common.domain.DataHelper.putAllRiskTradesInBulk;

public abstract class BaseBenchmark
{
    public abstract Supplier<IRiskTrade> tradeSupplier();

    public abstract Bucket<Integer, IRiskTrade> getWriteBucket();

    public abstract Bucket<Integer, IRiskTrade> getReadBucket();

    public void insertSingleTrades(int maxNumber)
    {
        IntStream.range(0, maxNumber).parallel().forEach(id ->
        {
            IRiskTrade riskTrade = createRiskTrade(tradeSupplier(), id);
            getWriteBucket().put(riskTrade.getId(), riskTrade);
        });
    }


    public void insertBulkTrades(int maxNumber, int batchSize)
    {
        int max = maxNumber-batchSize;
        for(int i = 0; i < max;)
        {
            putAllRiskTradesInBulk(getWriteBucket(), tradeSupplier(), i, batchSize);
            i = i + batchSize;
        }

    }

    public IRiskTrade getRandomTrade(int numberRecords)
    {
        int index = getRandomStartIndex(numberRecords);
        return getReadBucket().get(index);
    }

    public void populateReadCache(int maxNumber)
    {
        IntStream.range(1, maxNumber).parallel().forEach( id ->
        {
            IRiskTrade riskTrade = createRiskTrade(tradeSupplier(), id);
            getReadBucket().set(riskTrade.getId(), riskTrade);
        });

    }


}
