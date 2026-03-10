# Java 21 Benchmarks — Virtual Threads vs Platform Threads

Benchmarks con JMH midiendo el rendimiento real de Virtual Threads de Java 21
frente a Platform Threads tradicionales bajo carga I/O concurrente.

## Resultados

| Benchmark | Modo | Iteraciones | Tiempo promedio | Error |
|---|---|---|---|---|
| Platform Threads (pool 100) | avgt | 5 | 191.742 ms/op | ± 14.510 |
| Virtual Threads | avgt | 5 | 17.146 ms/op | ± 1.706 |

**Virtual Threads son 11x más rápidos** en escenarios de I/O concurrente con 1000 tareas simultáneas.

## ¿Qué mide este benchmark?

1000 tareas concurrentes que simulan operaciones I/O (sleep de 10ms cada una):

- **Platform Threads:** pool fijo de 100 threads — las tareas esperan turno
- **Virtual Threads:** un virtual thread por tarea — sin espera, sin bloqueo del carrier thread

## Entorno

- Java 21.0.9 (Eclipse Adoptium)
- JMH 1.37
- Windows 11 — AMD64
- Warmup: 3 iteraciones · Medición: 5 iteraciones

## Cómo correr los benchmarks
```bash
mvn clean package
java -jar target/benchmarks.jar
```

## Stack

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)
![JMH](https://img.shields.io/badge/JMH-1.37-blue?style=flat)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat&logo=apache-maven&logoColor=white)

## Autor

**Matías Martínez** — SRE & Backend Engineer  
[linkedin.com/in/ingmarma](https://linkedin.com/in/ingmarma) · [github.com/ingmarma](https://github.com/ingmarma)
