package com.geode.benchmark.common;

import org.apache.geode.cache.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GeodeBenchmarkHelper
{
    private static Logger logger = LoggerFactory.getLogger(GeodeBenchmarkHelper.class);

    public static <K, V> void removeBatch(Region<K, V> region, Set<K> keys, int batchSize)
    {
        Set<K> subset = new HashSet<>();
        Iterator iter = keys.iterator();

        while(iter.hasNext())
        {
            for(int i = 0; i < batchSize && iter.hasNext(); i++)
            {
                subset.add((K)iter.next());
            }
            region.removeAll(subset);
            subset.clear();
        }
        if(subset.size() > 0)
        {
            region.removeAll(subset);
            subset.clear();
        }
    }
}
