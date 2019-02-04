package common;

import java.util.Map;

public interface Bucket<K, V>
{
    V put(K key, V value);

    void set(K key, V value);

    V get(K key);

    void putAll(Map<K, V> keyValueMap);

    Map<K, V> getMap();
}
