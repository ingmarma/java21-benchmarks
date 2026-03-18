package com.ingmarma.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class GCBenchmark {

    private static final int OBJECT_COUNT = 100_000;
    private static final int STRING_SIZE = 1024;

    // Benchmark 1 — Allocación y GC de objetos pequeños
    @Benchmark
    public List<String> allocateShortLivedObjects() {
        List<String> list = new ArrayList<>(OBJECT_COUNT);
        for (int i = 0; i < OBJECT_COUNT; i++) {
            list.add("objeto-efimero-" + i);
        }
        return list;
    }

    // Benchmark 2 — Allocación de objetos grandes (presión en Old Gen)
    @Benchmark
    public List<byte[]> allocateLargeObjects() {
        List<byte[]> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(new byte[STRING_SIZE * 100]);
        }
        return list;
    }

    // Benchmark 3 — Mezcla de objetos corta y larga vida
    @Benchmark
    public String mixedAllocation() {
        List<String> shortLived = new ArrayList<>(1000);
        StringBuilder longLived = new StringBuilder();

        for (int i = 0; i < 1000; i++) {
            shortLived.add("efimero-" + i);
            if (i % 100 == 0) {
                longLived.append("persistente-").append(i).append("|");
            }
        }
        return longLived.toString();
    }

    // Benchmark 4 — String concatenation (genera mucha basura)
    @Benchmark
    public String stringConcatenation() {
        String result = "";
        for (int i = 0; i < 1000; i++) {
            result += "item-" + i;
        }
        return result;
    }

    // Benchmark 5 — StringBuilder (eficiente, menos presión GC)
    @Benchmark
    public String stringBuilder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("item-").append(i);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(GCBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
