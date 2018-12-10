package com.hazelcast.simulator.worker.loadsupport;

import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public final class MapStreamerFactory {

    private static Logger logger = LoggerFactory.getLogger(MapStreamerFactory.class);

    private static final AtomicBoolean CREATE_ASYNC;

    static {
        boolean createAsync = Boolean.valueOf(System.getProperty("benchmark.useAsyncMapStreamer", Boolean.FALSE.toString()));
        CREATE_ASYNC = new AtomicBoolean(createAsync);
    }

    private MapStreamerFactory() {
    }

    public static <K, V> MapStreamer<K, V> getInstance(IMap<K, V> map) {
        logger.info("Map streamer async mode: " + CREATE_ASYNC.get());
        return CREATE_ASYNC.get() ? new AsyncMapStreamer<>(map) : new SyncMapStreamer<>(map);
    }

    static void enforceAsync(boolean enforceAsync) {
        CREATE_ASYNC.set(enforceAsync);
    }
}
