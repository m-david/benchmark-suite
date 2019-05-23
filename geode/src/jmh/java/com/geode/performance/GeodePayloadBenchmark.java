package com.geode.performance;

import common.domain.EntryPayload;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static common.BenchmarkConstants.*;
import static common.BenchmarkUtility.getRandom;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode({Mode.SampleTime})
public class GeodePayloadBenchmark {

    private static final String POOL_NAME = "client-pool";

    public static final int PAYLOAD_SIZE_BYTES = Integer.valueOf(System.getProperty("benchmark.payload.size", "1000"));
    private static final String PAYLOAD_MAP                         = "payloadMap";
    private static final String PAYLOAD_PERSISTENT_OVERFLOW_MAP     = "payloadPersistentOverflowMap";
    private static final String PAYLOAD_PERSISTENT_MAP     = "payloadPersistentMap";
    private static final String PAYLOAD_OVERFLOW_MAP                = "payloadOverflowMap";
    private static final String PAYLOAD_OFFHEAP_MAP                = "payloadOffHeapMap";

    public static final String REGION_MODE = System.getProperty("benchmark.mode", "Normal");


    private static final Logger logger = LoggerFactory.getLogger(GeodePayloadBenchmark.class);

        private SecureRandom randomizer;
        private ClientCache clientCache;
        private PayloadMode mode;
        private Region<Integer, EntryPayload> payloadMap;

    private static ClientCacheFactory connectStandalone(String name)
    {
        return new ClientCacheFactory();
    }

    public void insertSingle(int maxNumber)
    {
        IntStream.range(0, maxNumber).forEach(id ->
        {
            byte[] bytes = new byte[PAYLOAD_SIZE_BYTES];
            randomizer.nextBytes(bytes);
            EntryPayload payload = new EntryPayload(bytes);
            payloadMap.put(id, payload);
        });
    }

    public void insertBulk(int maxNumber, int startIndex, int batchSize)
    {
        Map<Integer, EntryPayload> bulk = new HashMap<Integer, EntryPayload>();
        int limit = Math.min(maxNumber, startIndex+batchSize);

        IntStream.range(startIndex, limit).forEach(i ->
        {
            byte[] bytes = new byte[PAYLOAD_SIZE_BYTES];
            randomizer.nextBytes(bytes);
            EntryPayload payload = new EntryPayload(bytes);
            bulk.put(i, payload);
        });
        payloadMap.putAll(bulk);
    }

    public EntryPayload getRandomEntry(int numberRecords)
    {
        int index = getRandom();
        return payloadMap.get(index);
    }


    @Setup(Level.Trial)
    public void doSetup()
    {
        mode = PayloadMode.valueOf(REGION_MODE);

        logger.info("********************** doSetup: mode=" + mode.toString());

        ClientCacheFactory ccf = connectStandalone("my-test");
        ccf.setPoolSubscriptionEnabled(true);
        clientCache = ccf.create();

        randomizer = new SecureRandom();
        switch (mode)
        {
            case Normal:
                payloadMap = clientCache.getRegion(PAYLOAD_MAP);
                break;
            case Overflow:
                payloadMap = clientCache.getRegion(PAYLOAD_OVERFLOW_MAP);
                break;
            case PersistentOverflow:
                payloadMap = clientCache.getRegion(PAYLOAD_PERSISTENT_OVERFLOW_MAP);
                break;
            case Persistent:
                payloadMap = clientCache.getRegion(PAYLOAD_PERSISTENT_MAP);
                break;
            case OffHeap:
                payloadMap = clientCache.getRegion(PAYLOAD_OFFHEAP_MAP);
                break;
            default:
                payloadMap = clientCache.getRegion(PAYLOAD_MAP);
        }
    }

    // local runner for tests
    public static void main(String[] args) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(GeodePayloadBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(2)
                .forks(0)
                .jvmArgs("-ea")
                .shouldFailOnError(false) // switch to "true" to fail the complete run
                .build();

        new Runner(opt).run();

    }

    @Benchmark
    public void b01_InsertTradesSingle() throws Exception
    {
        insertSingle(NUMBER_OF_TRADES_TO_PROCESS);
    }

    @Benchmark
    public void b02_InsertTradesBulk(Blackhole blackhole) throws Exception
    {
        int max = NUMBER_OF_TRADES_TO_PROCESS - BATCH_SIZE;
        for(int i = 0; i < max;)
        {
            insertBulk(max, i, BATCH_SIZE);
            i = i + BATCH_SIZE;
        }
    }

    @Benchmark
    @Measurement(iterations = ITERATIONS, timeUnit = TimeUnit.MICROSECONDS)
    public void b03_GetTradeSingle(Blackhole blackhole) throws Exception
    {
        blackhole.consume(getRandomEntry(NUMBER_OF_TRADES_TO_PROCESS));
    }


}
