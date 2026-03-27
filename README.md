# Grocery Item Search (planned architecture)

This repository holds the **architecture and delivery plan** for a **microservices-based, natural-language search** capability over retail grocery inventory (name, description, department, and related fields). The approach favors **tokenization and inverted-index retrieval** (Elasticsearch-style analyzers, BM25, filters) similar in spirit to large-scale venue/event search, with **CDC-driven** incremental updates and room for **recipe-oriented** query handling.

## Status

**Planning only.** The full Spring Boot 3.x / Java 21 stack (query service, indexer, Kafka, Elasticsearch, optional Hadoop batch) is **not implemented** in this branch yet. Stakeholder **feedback on the design** is expected before implementation.

## Documentation

| Document | Purpose |
|----------|---------|
| [**SEARCH FEATURE.md**](./SEARCH%20FEATURE.md) | End-to-end architecture: microservices, CDC model, Kafka topics, caching, latency budgets, libraries, Maven layout, pitfalls, phased rollout |

## Planned stack (summary)

- **Runtime:** Java 21, Spring Boot 3.2+, Spring Cloud (Gateway, optional Config/Discovery)
- **Search:** Elasticsearch (+ Kibana for ops), tokenization via ES analyzers; Spring Data Elasticsearch or official Java API client
- **Streaming / CDC:** Apache Kafka; **Debezium** for capture from OLTP logical decoding; Spring Kafka for consumers
- **Cache:** Redis (shared) + Caffeine (in-process) for safe, scoped hot-query caching
- **Optional batch:** Hadoop HDFS / Spark for bulk rebuilds, history, and ranking features

## Repository layout (after implementation)

A multi-module Maven parent is planned (`search-query-service`, `search-indexer-service`, `enrichment-worker`, `shared-contracts`, etc.). Details and diagrams live in **SEARCH FEATURE.md**.

## License

See [LICENSE](./LICENSE).
