package com.hazelcast.performance;

import common.domain.RiskTrade;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class JavaSerializableBenchmark extends AbstractHazelcastUseCasesBenchmark {

    Supplier<RiskTrade> tradeSupplier() {
        return () -> new RiskTrade();
    }
}
