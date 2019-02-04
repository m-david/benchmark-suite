package com.hazelcast.performance;

import com.hazelcast.poc.domain.dataserializable.RiskTradeDataSerializable;
import common.domain.IRiskTrade;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class DataSerializableBenchmark extends AbstractHazelcastUseCasesBenchmark {

    public Supplier<IRiskTrade> tradeSupplier() {
        return () -> new RiskTradeDataSerializable();
    }

}
