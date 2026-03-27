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

## Architecture defaults (summary)

Aligned with common **cloud-native** practice; full rationale is in **SEARCH FEATURE.md §16**.

| Area | Default |
|------|---------|
| **OLTP reference** | **PostgreSQL** for v1 (Debezium logical decoding); other engines use the same outbox + topic contract via §6.5 if required. |
| **Hosting** | **Managed-first** Kafka, Connect (Debezium plugin), and Elasticsearch/OpenSearch where TCO allows. |
| **Multi-tenant search** | Shared cluster + **mandatory `tenant_id`** on documents and queries. |
| **Freshness** | Target **p99 pipeline ≤ ~5s** under normal load (tunable with ES refresh and bulk tuning). |
| **Data lake** | **Phase 1b** unless audit/regulation requires immediate topic→**S3/HDFS** mirror. |

## Observability (summary)

Industry-standard **OpenTelemetry** instrumentation in Spring Boot 3 (**Micrometer Observation** + OTLP), **OpenTelemetry Collector**, **Prometheus**-compatible metrics (including **JMX** for Kafka Connect/Debezium), **Grafana** (or cloud APM) for dashboards and SLOs, **Kibana/OpenSearch Dashboards** for Elasticsearch-native diagnostics, structured logs correlated with **trace_id**. See **SEARCH FEATURE.md §18** (mitigation catalog, monitoring checklist, starter SLOs).

## Documentation

| Document | Purpose |
|----------|---------|
| [**SEARCH FEATURE.md**](./SEARCH%20FEATURE.md) | Full design: **§16** resolved decisions, **§18** observability/mitigation/monitoring, **§17** relative effort, CDC (§6), refresh, topics, caching, Hadoop, testing |

## Industry alignment (summary)

Common **open-source** cores: **Kafka**, **Elasticsearch or OpenSearch**, **Debezium**, **transactional outbox**, **OpenTelemetry** for telemetry, optional **HDFS/S3 + Spark/Flink** for lake-scale history and reconciliation. Managed **Kafka Connect** (e.g. Confluent Cloud, MSK Connect) can run **Debezium** with less operational toil while keeping the same pattern.

## Planned stack (summary)

- **Runtime:** Java 21, Spring Boot 3.2+, Spring Cloud (Gateway, optional Config/Discovery)
- **Search:** Elasticsearch (+ Kibana) or OpenSearch; Spring Data Elasticsearch or official ES Java API client
- **Events / CDC:** **Transactional outbox** in **inventory-api-service**; **Debezium** (Kafka Connect) → Kafka; unwrap/SMT or relay; **Spring Kafka** consumers (enrichment, indexer)
- **Observability:** **OTel** + Micrometer, Collector, Prometheus/Mimir, Grafana, Tempo/Jaeger (or vendor APM); ES metrics/slowlog in Kibana
- **Batch / lake when needed:** **Hadoop HDFS** or **S3-compatible storage** with **Spark/Flink** for full rebuilds, archives, diff/reconciliation, and heavy analytics
- **Cache:** Redis (shared) + Caffeine (in-process), with strict cache-key rules for multi-tenant and pricing context

## Testing (planned)

Integration tests with **Testcontainers** (PostgreSQL, Kafka, Elasticsearch; **Debezium** / Connect where CI budget allows), plus **contract tests** on **unwrapped** outbox event schemas so indexer tests do not depend on raw Debezium envelopes.

## Repository layout (after implementation)

A multi-module Maven parent is planned (`search-query-service`, `search-indexer-service`, `enrichment-worker`, `inventory-api-service` or separate repo, `shared-contracts`, etc.). Details, diagrams, and tradeoff tables live in **SEARCH FEATURE.md**.

## License

See [LICENSE](./LICENSE).
