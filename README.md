# Grocery Item Search (planned architecture)

This repository holds the **architecture and delivery plan** for a **microservices-based, natural-language search** capability over retail grocery inventory (name, description, department, and related fields). The approach favors **tokenization and inverted-index retrieval** (Elasticsearch-style analyzers, BM25, filters)—the same **event-driven search index** pattern used across large retail and marketplace systems—with **incremental updates** driven by a **transactional outbox** and **Debezium** (Kafka Connect), **Hadoop-backed batch and reconciliation when volume or audit requires it**, and room for **recipe-oriented** query handling.

## Status

**Planning only.** The full Spring Boot 3.x / Java 21 stack is **not implemented** in this branch yet. Stakeholder **feedback on the design** is expected before implementation.

## CDC decision (summary)

| Aspect | Choice |
|--------|--------|
| **Publishing from inventory** | **Transactional outbox** — events written in the **same database transaction** as business mutations (no app-level dual write to Kafka). |
| **Capture to Kafka** | **Debezium** on **Kafka Connect** (log-based CDC from the OLTP WAL/binlog). |
| **Escape hatch** | Vendor or managed CDC **into the same Kafka topics** only if mandated; see **SEARCH FEATURE.md §6.5**. |

## Documentation

| Document | Purpose |
|----------|---------|
| [**SEARCH FEATURE.md**](./SEARCH%20FEATURE.md) | Full design: **outbox + Debezium** stack detail, **§17 relative effort** for key features, microservices, CDC depth (sharding, refresh), Kafka topics, caching, latency budgets, industry context, Hadoop/lake, testing |

## Industry alignment (summary)

Common **open-source** cores in analogous catalog/search estates: **Kafka**, **Elasticsearch or OpenSearch**, **Debezium** for log-based CDC, and **transactional outbox** for reliable publication from microservices—plus optional **HDFS/S3 + Spark/Flink** for snapshots, reconciliation, and ML features. Managed **Kafka Connect** (e.g. Confluent Cloud, MSK Connect) can run **Debezium** with less operational toil while keeping the same pattern.

## Planned stack (summary)

- **Runtime:** Java 21, Spring Boot 3.2+, Spring Cloud (Gateway, optional Config/Discovery)
- **Search:** Elasticsearch (+ Kibana) or OpenSearch; Spring Data Elasticsearch or official ES Java API client
- **Events / CDC:** **Transactional outbox** in **inventory-api-service**; **Debezium** (Kafka Connect) → Kafka; unwrap/SMT or relay for clean topic payloads; **Spring Kafka** consumers (enrichment, indexer)
- **Batch / lake when needed:** **Hadoop HDFS** or **S3-compatible storage** with **Spark/Flink** for full rebuilds, archives, diff/reconciliation, and heavy analytics
- **Cache:** Redis (shared) + Caffeine (in-process), with strict cache-key rules for multi-tenant and pricing context

## Testing (planned)

Integration tests with **Testcontainers** (PostgreSQL, Kafka, Elasticsearch; **Debezium** / Connect where CI budget allows), plus **contract tests** on **unwrapped** outbox event schemas so indexer tests do not depend on raw Debezium envelopes.

## Repository layout (after implementation)

A multi-module Maven parent is planned (`search-query-service`, `search-indexer-service`, `enrichment-worker`, `inventory-api-service` or separate repo, `shared-contracts`, etc.). Details, diagrams, and tradeoff tables live in **SEARCH FEATURE.md**.

## License

See [LICENSE](./LICENSE).
