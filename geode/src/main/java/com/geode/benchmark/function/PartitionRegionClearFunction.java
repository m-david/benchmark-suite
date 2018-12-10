package com.geode.benchmark.function;

import com.geode.benchmark.common.GeodeBenchmarkHelper;
import org.apache.geode.LogWriter;
import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;

import java.util.*;

public class PartitionRegionClearFunction implements Function, Declarable
{
    public final static String ID = "PartitionRegionClearFunction";

    private static final short VERSION = 3;

    private Cache cache;

//    private static Logger logger =
        //LoggerFactory.getLogger(PartitionRegionClearFunction.class);

    @Override
    public void initialize(Cache cache, Properties properties)
    {
        this.cache = cache;
    }

    @Override
    public void execute(FunctionContext context)
    {
        LogWriter logger = cache.getLogger();
        logger.info(String.format("PartitionRegionClearFunction{%s}.execute...", ""+VERSION));
        Object[] functionArgs = (Object[]) context.getArguments();
        String regionPath;
        int batchSize = 500;
        if(functionArgs.length < 1)
        {
            logger.info("no regionPath");
            return;
        }
        regionPath = (String) functionArgs[0];
        logger.info("regionPath: " + regionPath);

        if(functionArgs.length > 1)
        {
            try
            {
                batchSize = Integer.valueOf((String) functionArgs[1]);
            }
            catch (NumberFormatException e)
            {
                //do nothing
            }
        }

        Region<Object, Object> region = context.getCache().getRegion(regionPath);

        logger.info("before remove, size: " + region.size());
        if(!PartitionRegionHelper.isPartitionedRegion(region))
        {
            return;
        }

        Region<Object, Object> dataSet = PartitionRegionHelper.getLocalData(region);
        logger.info("localDataSet.size: " + dataSet.size());

        Set<Object> keys = dataSet.keySet();

        GeodeBenchmarkHelper.removeBatch(region, keys, batchSize);

//        keys.forEach(k -> region.remove(k));
        logger.info("after remove, region    size: " + region.size());
        logger.info("after remove, localData size: " + dataSet.size());
        logger.info("completed.");
    }

    @Override
    public boolean hasResult()
    {
        return false;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean optimizeForWrite()
    {
        return true;
    }

    @Override
    public boolean isHA()
    {
        return false;
    }

}
