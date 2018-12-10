package com.hazelcast.simulator.worker.loadsupport;

import com.hazelcast.core.IMap;

public class SyncMapStreamer<K, V> implements MapStreamer<K, V> {

    private final IMap<K, V> map;

    public SyncMapStreamer(IMap<K, V> map) {
        this.map = map;
    }

    @Override
    public void pushEntry(K key, V value) {
        map.put(key, value);
    }

    @Override
    public void close() {
    }
}
