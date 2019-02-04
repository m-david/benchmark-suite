package com.hazelcast.performance.support;

import com.hazelcast.core.IMap;
import common.BucketImpl;

public class MapWrapper<K, V> extends BucketImpl<K, V>
{

    public MapWrapper(IMap<K, V> map)
    {
        super(map);
    }

    @Override
    public void set(K key, V value) {
        ((IMap<K, V>) getMap()).set(key, value);
    }
}
