package com.ingmarma.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class CompletableFutureBenchmark {

    private static final int TASK_COUNT = 1000;

    // Benchmark 1 — Virtual Threads (S1, base de comparación)
    @Benchmark
    public void virtualThreads() throws InterruptedException {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(TASK_COUNT);
            for (int i = 0; i < TASK_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }
    }

    // Benchmark 2 — CompletableFuture con ForkJoinPool común
    @Benchmark
    public void completableFutureCommonPool() throws Exception {
        List<CompletableFuture<Void>> futures = IntStream.range(0, TASK_COUNT)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
    }

    // Benchmark 3 — CompletableFuture con Virtual Threads executor
    @Benchmark
    public void completableFutureVirtualThreads() throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, TASK_COUNT)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        }
    }

    // Benchmark 4 — CompletableFuture con chain y timeout
    @Benchmark
    public String completableFutureChainWithTimeout() throws Exception {
        return CompletableFuture
                .supplyAsync(() -> {
                    try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return "paso-1";
                })
                .thenApplyAsync(result -> {
                    try { Thread.sleep(5); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return result + " → paso-2";
                })
                .thenApplyAsync(result -> result + " → paso-3")
                .orTimeout(500, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> "fallback: " + ex.getMessage())
                .get();
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(CompletableFutureBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
