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
diferente presión sobre el G1GC. Cada += crea un nuevo objeto String en el
heap. 1000 iteraciones = 1000 objetos efímeros que el GC tiene que recolectar.

GC utilizado: G1GC con `-Xms512m -Xmx512m`

---

## Conclusiones

- Virtual Threads son 11x más rápidos que Platform Threads en I/O concurrente
- CompletableFuture con ForkJoinPool común es 95x más lento que con Virtual Threads — el executor importa más que la API
- CompletableFuture + Virtual Threads executor tiene rendimiento equivalente a Virtual Threads puros — usá CF para composición async (chain, timeout, fallback)
- String += en loop genera 75x más presión sobre el GC que StringBuilder — cada concatenación crea un objeto nuevo en el heap
- Objetos efímeros en Young Gen y objetos grandes en Old Gen generan pausas de ~1.5ms — invisibles pero acumuladas en producción

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
