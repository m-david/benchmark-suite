package com.geode.benchmark.function;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;

import java.util.Properties;

public class CreateIndexFunction implements Function, Declarable
{
    public static final String ID = CreateIndexFunction.class.getName();

    private Cache cache;

    @Override
    public void execute(FunctionContext context)
    {
        Object[] functionArgs = (Object[]) context.getArguments();
        if(functionArgs == null || functionArgs.length < 3)
        {
            return;
        }
        String indexName = (String) functionArgs[0];
        String queryString = (String) functionArgs[1];
        String regionPath = (String) functionArgs[2];

        Region<?, ?> region = cache.getRegion(regionPath);
        if(cache.getQueryService().getIndex(region, indexName) == null)
        {
            try
            {
                cache.getQueryService().createIndex(indexName, queryString, regionPath);
                context.getResultSender().lastResult("IndexCreated");
            }
            catch (Exception e)
            {
                cache.getLogger().error(e);
                context.getResultSender().lastResult("IndexCreationError");
            }
        }
        else
        {
            context.getResultSender().lastResult("IndexExists");
        }

    }

    @Override
    public void initialize(Cache cache, Properties properties)
    {
        this.cache = cache;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public boolean isHA()
    {
        return true;
    }

    @Override
    public boolean optimizeForWrite()
    {
        return true;
    }
}
