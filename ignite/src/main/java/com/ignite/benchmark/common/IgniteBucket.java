package com.ignite.benchmark.common;

import common.Bucket;
import org.apache.ignite.client.ClientCache;

import java.util.Map;

public class IgniteBucket<Integer, IRiskTrade> implements Bucket<Integer, IRiskTrade>
{
    private ClientCache<Integer, IRiskTrade> map;

    public IgniteBucket(ClientCache<Integer, IRiskTrade> map)
    {
        this.map = map;
    }

    @Override
    public IRiskTrade put(Integer key, IRiskTrade value) {
        map.put(key, value);
        return value;
    }

    @Override
    public void set(Integer key, IRiskTrade value) {
        map.put(key, value);
    }

    @Override
    public IRiskTrade get(Integer key) {
        return map.get(key);
    }

    @Override
    public void putAll(Map<Integer, IRiskTrade> keyValueMap) {
        map.putAll(keyValueMap);
    }

    @Override
    public Map<Integer, IRiskTrade> getMap() {
        return null;
    }
}
