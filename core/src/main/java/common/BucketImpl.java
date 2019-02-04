package common;

import java.util.Map;

public class BucketImpl<K, V> implements Bucket<K, V>
{
    private Map<K, V> map;

    public BucketImpl(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    @Override
    public void set(K key, V value) {
        map.put(key, value);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public void putAll(Map<K, V> keyValueMap)
    {
        map.putAll(keyValueMap);
    }

    public Map<K, V> getMap()
    {
        return map;
    }
}
