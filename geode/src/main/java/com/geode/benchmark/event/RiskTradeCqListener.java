package com.geode.benchmark.event;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.query.CqEvent;
import org.apache.geode.cache.util.CqListenerAdapter;

public class RiskTradeCqListener extends CqListenerAdapter
{
    @Override
    public void onEvent(CqEvent aCqEvent)
    {
        CacheFactory.getAnyInstance().getLogger().info("CQC listener value: " + aCqEvent.getNewValue());
    }
}
