# Java 21 Benchmarks — Concurrencia y Performance

Benchmarks con JMH midiendo el rendimiento real de las APIs de concurrencia
de Java 21 bajo carga I/O concurrente. Proyecto en construcción — se agrega
un benchmark nuevo cada semana.

---

## S1 — Virtual Threads vs Platform Threads

| Benchmark | Tiempo promedio | Error |
|---|---|---|
| Platform Threads (pool 100) | 191.742 ms/op | ± 14.510 |
| Virtual Threads | 17.146 ms/op | ± 1.706 |

**Virtual Threads son 11x más rápidos** con 1000 tareas I/O concurrentes.

---

## S2 — CompletableFuture vs Virtual Threads

| Benchmark | Tiempo promedio | Error |
|---|---|---|
| CF + ForkJoinPool común | 1492.983 ms/op | ± 170.874 |
| CF + Virtual Threads executor | 15.691 ms/op | ± 0.455 |
| Virtual Threads puros | 16.157 ms/op | ± 3.177 |
| CF chain con timeout/fallback | 12.617 ms/op | ± 1.989 |

**CompletableFuture con ForkJoinPool común es 95x más lento** que con
Virtual Threads. El executor debajo importa más que la API que usás.

---

## S3 — GC Pressure — Cómo escribís código importa

| Benchmark | Tiempo promedio | Error |
|---|---|---|
| allocateShortLivedObjects (100k objetos efímeros) | 1.414 ms/op | ± 0.065 |
| allocateLargeObjects (objetos grandes, Old Gen) | 1.507 ms/op | ± 0.041 |
| stringConcatenation (+= en loop, 1000 iter) | 0.682 ms/op | ± 0.039 |
| mixedAllocation (corta + larga vida) | 0.011 ms/op | ± 0.001 |
| stringBuilder (StringBuilder.append, 1000 iter) | 0.009 ms/op | ± 0.001 |

**String += en loop es 75x más lento que StringBuilder** — mismo resultado,
diferente presión sobre el G1GC.

---

## S4 — Profiling — Lo que los números rompen de tus intuiciones

| Benchmark | Tiempo promedio | Error | Unidad |
|---|---|---|---|
| stringJoinBuilder | 15.576 | ± 1.452 | µs/op |
| stringJoinStream | 25.273 | ± 2.550 | µs/op |
| linearSearch (stream filter) | 76.282 | ± 8.336 | µs/op |
| sortWithParallelStream | 3.904 | ± 1.193 | ms/op |
| hashSetSearch (new HashSet + contains) | 4.480 | ± 1.986 | ms/op |
| sortWithCollections | 18.501 | ± 3.063 | ms/op |
| sortWithStream | 18.452 | ± 1.347 | ms/op |

**HashSet es 59x más lento que búsqueda lineal** en este caso — O(1) no
significa "siempre más rápido". El costo de construcción importa.

**Parallel Stream es 4.7x más rápido** que Collections.sort para 100k elementos.

---

## S5 — Concurrencia Avanzada — synchronized vs Lock vs Atomic vs Virtual Threads

| Benchmark | Tiempo promedio | Error | Unidad |
|---|---|---|---|
| virtualThreadsAtomicCounter | 5.525 | ± 0.352 | ms/op |
| countDownLatchCoordination | 15.118 | ± 0.381 | ms/op |
| phaserCoordination | 15.106 | ± 1.191 | ms/op |
| reentrantLockCounter | 15.387 | ± 4.421 | ms/op |
| synchronizedCounter | 24.829 | ± 8.520 | ms/op |
| atomicCounter (pool fijo) | 27.984 | ± 7.952 | ms/op |

**Virtual Threads + AtomicInteger es 5x más rápido que synchronized** con
100 threads concurrentes incrementando un contador 1000 veces cada uno.
AtomicInteger con pool fijo es el más lento — el overhead de gestión del
pool supera el beneficio lock-free en este escenario.

---

## Conclusiones acumuladas

- Virtual Threads son 11x más rápidos que Platform Threads en I/O concurrente
- CompletableFuture con ForkJoinPool común es 95x más lento que con Virtual Threads — el executor importa más que la API
- String += en loop genera 75x más presión sobre el GC que StringBuilder
- HashSet no siempre es más rápido — el costo de construcción puede superar el beneficio de O(1)
- Parallel Stream es 4.7x más rápido que sort secuencial para colecciones grandes
- Virtual Threads + AtomicInteger es 5x más rápido que synchronized en concurrencia alta
- CountDownLatch y Phaser tienen rendimiento equivalente para coordinación (~15ms)

---

## Entorno

- Java 21.0.9 (Eclipse Adoptium)
- JMH 1.37 · Maven 3.9
- Windows 11 — AMD64
- Warmup: 3 iteraciones · Medición: 5 iteraciones

## Cómo correr los benchmarks
```bash
mvn clean package

# Todos los benchmarks
java -jar target/benchmarks.jar

# Benchmark específico
java -jar target/benchmarks.jar VirtualThreadsBenchmark
java -jar target/benchmarks.jar CompletableFutureBenchmark
java -jar target/benchmarks.jar GCBenchmark
java -jar target/benchmarks.jar ProfilingBenchmark
java -jar target/benchmarks.jar ConcurrencyBenchmark

# GCBenchmark con logs de GC
java -Xms512m -Xmx512m -XX:+UseG1GC -Xlog:gc*:file=gc_g1.log \
  -jar target/benchmarks.jar GCBenchmark
```

## Stack

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![JMH](https://img.shields.io/badge/JMH-1.37-blue?style=flat)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat&logo=apache-maven&logoColor=white)

## Autor

**Matías Martínez** — SRE & Backend Engineer  
[linkedin.com/in/ingmarma](https://linkedin.com/in/ingmarma) · [github.com/ingmarma](https://github.com/ingmarma)