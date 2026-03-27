# Grocery Item Search (planned architecture)

This repository holds the **architecture and delivery plan** for a **microservices-based, natural-language search** capability over retail grocery inventory (name, description, department, and related fields). The approach favors **tokenization and inverted-index retrieval** (Elasticsearch-style analyzers, BM25, filters)—the same **event-driven search index** pattern used across large retail and marketplace systems—with **CDC-driven** incremental updates, **Hadoop-backed batch and reconciliation when volume or audit requires it**, and room for **recipe-oriented** query handling.

## Status

**Planning only.** The full Spring Boot 3.x / Java 21 stack is **not implemented** in this branch yet. Stakeholder **feedback on the design** is expected before implementation.

## Documentation

| Document | Purpose |
|----------|---------|
| [**SEARCH FEATURE.md**](./SEARCH%20FEATURE.md) | Full design: microservices, **CDC in depth** (process, Kafka/ES sharding, Debezium **alternatives** and enterprise tradeoffs), **index refresh in depth**, Kafka topics, caching, latency budgets, **industry stacks & OSS**, long-term weaknesses and scale limits, **Hadoop/lake** usage, testing with Testcontainers, Maven layout, phased rollout |

## Industry alignment (summary)

Common **open-source** cores in analogous catalog/search estates: **Kafka** for durable change streams, **Elasticsearch or OpenSearch** for tokenized search, **log-based CDC** (often **Debezium** on Kafka Connect) or **transactional outbox + CDC**, and optional **HDFS/S3 + Spark/Flink** for snapshots, reconciliation, and ML features. Enterprises often pair these with **managed** Kafka/Elastic/CDC (Confluent, cloud providers, GoldenGate-class tools) while keeping **downstream topic contracts** stable for long-term evolution.

## Planned stack (summary)

- **Runtime:** Java 21, Spring Boot 3.2+, Spring Cloud (Gateway, optional Config/Discovery)
- **Search:** Elasticsearch (+ Kibana) or OpenSearch; Spring Data Elasticsearch or official ES Java API client
- **Streaming / CDC:** Apache Kafka; **Debezium** or **enterprise CDC → Kafka**; **transactional outbox** where microservice boundaries require it; Spring Kafka consumers
- **Batch / lake when needed:** **Hadoop HDFS** or **S3-compatible storage** with **Spark/Flink** for full rebuilds, archives, diff/reconciliation, and heavy analytics
- **Cache:** Redis (shared) + Caffeine (in-process), with strict cache-key rules for multi-tenant and pricing context

## Testing (planned)

Integration tests with **Testcontainers** (Kafka, PostgreSQL, Elasticsearch; **Debezium Testcontainers** when validating capture), plus **contract tests** on Avro/JSON event fixtures so indexer behavior survives **CDC vendor** or **connector** changes.

## Repository layout (after implementation)

A multi-module Maven parent is planned (`search-query-service`, `search-indexer-service`, `enrichment-worker`, `shared-contracts`, etc.). Details, diagrams, and tradeoff tables live in **SEARCH FEATURE.md**.

## License

See [LICENSE](./LICENSE).
