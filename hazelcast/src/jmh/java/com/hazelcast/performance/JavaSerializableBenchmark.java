package com.hazelcast.performance;

import common.domain.IRiskTrade;
import common.domain.RiskTrade;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JavaSerializableBenchmark extends AbstractHazelcastUseCasesBenchmark {

    public Supplier<IRiskTrade> tradeSupplier() {
        return () -> new RiskTrade();
    }
}
