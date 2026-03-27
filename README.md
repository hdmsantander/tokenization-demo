# Grocery Item Search (planned architecture)

This repository holds the **architecture and delivery plan** for a **microservices-based, natural-language search** capability over retail grocery inventory (name, description, department, and related fields). The approach favors **tokenization and inverted-index retrieval** (Elasticsearch-style analyzers, BM25, filters)—the same **event-driven search index** pattern used across large retail and marketplace systems—with **incremental updates** driven by a **transactional outbox** and **Debezium** (Kafka Connect), **Hadoop-backed batch and reconciliation when volume or audit requires it**, and room for **recipe-oriented** query handling.

The design is **cloud-agnostic**. **Microsoft Azure** and **Google Cloud** integration options are mapped in **SEARCH FEATURE.md §21** (managed Postgres, Kafka, object storage, AKS/GKE, observability, secrets).

## Status

**Implementation started (skeleton).** Multi-module **Java 21** / **Spring Boot 3.4** apps, **JSON Schema** contracts, **Maven Wrapper**, **GitHub Actions CI**, and **Compose D0** (Postgres + Elasticsearch + Redis) are in place. Business features (Flyway, Kafka indexer, Debezium) follow [**SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md**](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md) **§10**.

Architecture and demo scope remain in **SEARCH FEATURE.md** (**§15**, **§19**). **Demo v1 defaults** are frozen in the plan (**§3**).

## Quickstart

```bash
./scripts/onboard.sh
```

This runs **`./mvnw verify`** (falls back to `mvn` if the wrapper is unavailable), which includes contract tests and—when Docker is available—Testcontainers checks for Elasticsearch and PostgreSQL.

## Local infrastructure (Docker)

```bash
docker compose -f compose/docker-compose.infra.yml up -d
./scripts/validate-infra.sh
```

Details: [**compose/README.md**](./compose/README.md).

Run Spring services against Docker hosts using profile **`docker`** (see each module’s `application-docker.yml`). Example:

```bash
./mvnw -pl search-query-service spring-boot:run -Dspring-boot.run.profiles=docker
```

## Development

| Step | Action |
|------|--------|
| 1 | Complete pre-implementation items in plan **§9** where still relevant (e.g. Elastic license check for your org). |
| 2 | Follow implementation backlog **§10**; current repo state completes **A.1–A.6** (skeleton + tests) and **D0** (infra Compose). |
| 3 | Target **~26–49 person-days** for the full demo vertical slice (plan **§7**). |

**Prerequisites:** JDK **21**, **Docker** + Compose for infra scripts and integration tests, **≥ 16 GB RAM** recommended when Kafka/Connect are added (**SEARCH FEATURE.md §19.2**).

## Demo and implementation scope

| Scope | Description |
|-------|-------------|
| **Reference demo (when code exists)** | **Docker** images and **Docker Compose** for local run and optional CI smoke—see **SEARCH FEATURE.md §19** |
| **Production-style runtime (proposed)** | **Kubernetes** (any CNCF distro: **AKS**, **GKE**, etc.)—see **§20** |
| **Managed cloud** | Optional mapping tables for **Azure** and **GCP** in **§21** |

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
| **Hosting** | **Managed-first** Kafka, Connect (Debezium plugin), and Elasticsearch/OpenSearch where TCO allows; **§21** lists Azure/GCP names. |
| **Multi-tenant search** | Shared cluster + **mandatory `tenant_id`** on documents and queries. |
| **Freshness** | Target **p99 pipeline ≤ ~5s** under normal load (tunable with ES refresh and bulk tuning). |
| **Data lake** | **Phase 1b** unless audit/regulation requires immediate topic→**S3/HDFS**/Blob/GCS mirror. |

## Observability (summary)

Industry-standard **OpenTelemetry** instrumentation in Spring Boot 3 (**Micrometer Observation** + OTLP), **OpenTelemetry Collector**, **Prometheus**-compatible metrics (including **JMX** for Kafka Connect/Debezium), **Grafana** (or cloud APM) for dashboards and SLOs, **Kibana/OpenSearch Dashboards** for Elasticsearch-native diagnostics, structured logs correlated with **trace_id**. **Azure Monitor / Application Insights** and **Google Cloud Observability** are listed as OTLP/managed backends in **§18** and **§21**. See **SEARCH FEATURE.md §18** (mitigation catalog, monitoring checklist, starter SLOs).

## Documentation

| Document | Purpose |
|----------|---------|
| [**SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md**](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md) | Validated layers, demo v1 decisions (**§3**), person-day estimates (**§7**), pre-implementation checklist (**§9**), agent backlog (**§10**), Compose **D0** vs full stack |
| [**SEARCH FEATURE.md**](./SEARCH%20FEATURE.md) | Full design: **§3.4–§3.5** industry parallels & anti-pattern validation, **§16** defaults, **§17** effort, **§18** observability, **§19** Docker Compose, **§20** Kubernetes, **§21** Azure & GCP, CDC (§6), refresh, topics, Hadoop, testing |

## Industry alignment (summary)

Common **open-source** cores: **Kafka**, **Elasticsearch or OpenSearch**, **Debezium**, **transactional outbox**, **OpenTelemetry**, optional **object store + Spark/Flink** for lake-scale history. **Docker Compose** for demo; **Kubernetes** + managed services for scale.

The design matches **CQRS-style** product/catalog systems (OLTP for writes, search index for reads) described in industry write-ups—**PostgreSQL → CDC/outbox → Kafka → Elasticsearch** is a well-documented pattern; see **SEARCH FEATURE.md §3.4** for **quality attributes** (freshness, eventual consistency, operability) and **§3.5** for **anti-patterns avoided** (dual write, silent pipeline stall, treating ES as source of truth).

## Planned stack (summary)

- **Runtime:** Java 21, Spring Boot 3.2+, Spring Cloud (Gateway, optional Config/Discovery)
- **Local demo:** **Dockerfile** per service + **`docker-compose.yml`** (Postgres, Kafka, Connect+Debezium, ES, Redis, apps)—**§19**
- **Search:** Elasticsearch (+ Kibana) or OpenSearch; Spring Data Elasticsearch or official ES Java API client
- **Events / CDC:** **Transactional outbox** in **inventory-api-service**; **Debezium** (Kafka Connect) → Kafka; unwrap/SMT or relay; **Spring Kafka** consumers (enrichment, indexer)
- **Observability:** **OTel** + Micrometer, Collector, Prometheus/Mimir, Grafana, Tempo/Jaeger (or **Azure Monitor** / **GCP** backends—**§21**)
- **Batch / lake when needed:** **HDFS** or **cloud object storage** (S3, **Azure Blob**, **GCS**) with **Spark/Flink** for full rebuilds, archives, diff/reconciliation
- **Cache:** Redis (shared) + Caffeine (in-process), with strict cache-key rules for multi-tenant and pricing context

## Testing (planned)

Integration tests with **Testcontainers** (PostgreSQL, Kafka, Elasticsearch; **Debezium** / Connect where CI budget allows), plus **contract tests** on **unwrapped** outbox event schemas so indexer tests do not depend on raw Debezium envelopes. Optional **Compose** smoke job (**§19**).

## Repository layout

| Path | Purpose |
|------|---------|
| `shared-contracts/` | JSON Schemas + fixture-based contract tests |
| `inventory-api-service/` | Port **8080** — inventory API (outbox in Stage C) |
| `search-query-service/` | Port **8081** — search API (Elasticsearch in Stage B) |
| `search-indexer-service/` | Port **8082** — Kafka → ES indexer (Stage C) |
| `compose/docker-compose.infra.yml` | D0: Postgres, Elasticsearch, Redis |
| `scripts/onboard.sh` | Developer onboarding + `mvnw verify` |
| `scripts/validate-infra.sh` | Health checks when Compose D0 is running |

A full **`docker-compose.yml`** (Kafka + Connect + Debezium + apps) and optional **`k8s/`** remain per **SEARCH FEATURE.md §19–§20**.

## License

See [LICENSE](./LICENSE).
