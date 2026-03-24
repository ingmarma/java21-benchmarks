package com.ingmarma.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ProfilingBenchmark {

    private List<Integer> data;

    @Setup
    public void setup() {
        Random random = new Random(42);
        data = IntStream.range(0, 100_000)
                .mapToObj(i -> random.nextInt(100_000))
                .collect(Collectors.toList());
    }

    // Benchmark 1 — Sort con Collections (baseline)
    @Benchmark
    public List<Integer> sortWithCollections() {
        List<Integer> copy = new ArrayList<>(data);
        Collections.sort(copy);
        return copy;
    }

    // Benchmark 2 — Sort con Stream
    @Benchmark
    public List<Integer> sortWithStream() {
        return data.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    // Benchmark 3 — Sort con Stream paralelo
    @Benchmark
    public List<Integer> sortWithParallelStream() {
        return data.parallelStream()
                .sorted()
                .collect(Collectors.toList());
    }

    // Benchmark 4 — Búsqueda lineal (O(n))
    @Benchmark
    public long linearSearch() {
        int target = 50_000;
        return data.stream()
                .filter(x -> x == target)
                .count();
    }

    // Benchmark 5 — Búsqueda en HashSet (O(1))
    @Benchmark
    public boolean hashSetSearch() {
        Set<Integer> set = new HashSet<>(data);
        return set.contains(50_000);
    }

    // Benchmark 6 — String join con stream
    @Benchmark
    public String stringJoinStream() {
        return data.stream()
                .limit(1000)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    // Benchmark 7 — String join con StringBuilder
    @Benchmark
    public String stringJoinBuilder() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Integer n : data) {
            if (count++ >= 1000) break;
            if (sb.length() > 0) sb.append(",");
            sb.append(n);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ProfilingBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}