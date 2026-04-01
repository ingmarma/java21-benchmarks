package com.ingmarma.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ConcurrencyBenchmark {

    private static final int THREADS = 100;
    private static final int INCREMENTS = 1000;

    // Benchmark 1 — synchronized (clásico)
    @Benchmark
    public int synchronizedCounter() throws InterruptedException {
        final int[] counter = {0};
        CountDownLatch latch = new CountDownLatch(THREADS);
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS; j++) {
                        synchronized (counter) {
                            counter[0]++;
                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }
        return counter[0];
    }

    // Benchmark 2 — ReentrantLock
    @Benchmark
    public int reentrantLockCounter() throws InterruptedException {
        final int[] counter = {0};
        ReentrantLock lock = new ReentrantLock();
        CountDownLatch latch = new CountDownLatch(THREADS);
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS; j++) {
                        lock.lock();
                        try {
                            counter[0]++;
                        } finally {
                            lock.unlock();
                        }
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }
        return counter[0];
    }

    // Benchmark 3 — AtomicInteger (lock-free)
    @Benchmark
    public int atomicCounter() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(THREADS);
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS; j++) {
                        counter.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }
        return counter.get();
    }

    // Benchmark 4 — Virtual Threads + AtomicInteger
    @Benchmark
    public int virtualThreadsAtomicCounter() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(THREADS);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    for (int j = 0; j < INCREMENTS; j++) {
                        counter.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            latch.await();
        }
        return counter.get();
    }

    // Benchmark 5 — CountDownLatch coordinación
    @Benchmark
    public void countDownLatchCoordination() throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREADS);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                executor.submit(() -> {
                    try {
                        startSignal.await();
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneSignal.countDown();
                    }
                });
            }
            startSignal.countDown();
            doneSignal.await();
        }
    }

    // Benchmark 6 — Phaser coordinación
    @Benchmark
    public void phaserCoordination() throws InterruptedException {
        Phaser phaser = new Phaser(1);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < THREADS; i++) {
                phaser.register();
                executor.submit(() -> {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                });
            }
            phaser.arriveAndAwaitAdvance();
        }
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(ConcurrencyBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
