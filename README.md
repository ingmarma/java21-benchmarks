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

## Conclusiones

- Virtual Threads y CompletableFuture con Virtual Threads executor tienen
  rendimiento equivalente (~15-17 ms/op)
- El ForkJoinPool común es el cuello de botella real en I/O concurrente —
  limita la concurrencia a la cantidad de CPUs disponibles
- CompletableFuture sigue siendo útil para composición async (chain, timeout,
  fallback) — combinado con Virtual Threads es la mejor opción

---

## Entorno

- Java 21.0.9 (Eclipse Adoptium)
- JMH 1.37 · Maven 3.9
- Windows 11 — AMD64
- Warmup: 3 iteraciones · Medición: 5 iteraciones · 1000 tareas I/O

## Cómo correr los benchmarks
```bash
mvn clean package

# Todos los benchmarks
java -jar target/benchmarks.jar

# Benchmark específico
java -jar target/benchmarks.jar VirtualThreadsBenchmark
java -jar target/benchmarks.jar CompletableFutureBenchmark
```

## Stack

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![JMH](https://img.shields.io/badge/JMH-1.37-blue?style=flat)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat&logo=apache-maven&logoColor=white)

## Autor

**Matías Martínez** — SRE & Backend Engineer  
[linkedin.com/in/ingmarma](https://linkedin.com/in/ingmarma) · [github.com/ingmarma](https://github.com/ingmarma)
