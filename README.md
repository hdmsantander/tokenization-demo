# Grocery Item Search (planned architecture)

This repository holds the **architecture** and a **first-draft implementation ecosystem** for a **microservices-based, natural-language search** capability over retail grocery inventory (name, description, department, and related fields). The approach favors **tokenization and inverted-index retrieval** (Elasticsearch-style analyzers, BM25, filters)—the same **event-driven search index** pattern used across large retail and marketplace systems—with **incremental updates** driven by a **transactional outbox** and **Debezium** (Kafka Connect), **Hadoop-backed batch and reconciliation when volume or audit requires it**, and room for **recipe-oriented** query handling.

**First draft (code in repo):** runnable **Maven** multi-module skeleton, **JSON Schema** contracts, **Docker Compose D0** (Postgres + Elasticsearch + Redis), **CI**, and onboarding scripts. **Kafka, Connect, Debezium, and real search/indexing** are **not** implemented yet—see [**docs/IMPLEMENTATION.md**](./docs/IMPLEMENTATION.md) for the **current step** and **next steps**.

The design is **cloud-agnostic**. **Microsoft Azure** and **Google Cloud** integration options are mapped in **SEARCH FEATURE.md** (section 21 — managed Postgres, Kafka, object storage, AKS/GKE, observability, secrets).

## Status

**Implementation: first draft ecosystem (Stage A + D0 done; Stage B next).**

| Where | Detail |
|-------|--------|
| **Current step** | End of **Stage A** (foundations); **next: Stage B** — Elasticsearch mapping, seed, query API |
| **Canonical pointer** | [**docs/IMPLEMENTATION.md**](./docs/IMPLEMENTATION.md) — flow diagram, ecosystem inventory, ports, validation commands |
| **Backlog order** | [SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md section 10](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md#10-implementation-backlog-for-the-coding-agent--execute-after-9) |

Architecture and demo scope: **SEARCH FEATURE.md** (phases 15–19). **Demo v1 defaults:** [implementation plan section 3](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md#3-demo-v1-decisions-frozen-for-development).

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

## Implementation flow (summary)

```text
Done:     A.1–A.3, A.5, A.6  +  D0 (compose/docker-compose.infra.yml)
Next:     B.1 → B.2 → B.3   (read path: ES mapping, seed, query API)
Later:    C (outbox + Debezium + indexer)  →  D1 (full compose)  →  E (docs polish)
```

**Note:** Stage **A.4** (Kafka listener stub in indexer) is optional before **B**; see **docs/IMPLEMENTATION.md**.

## Development

| Step | Action |
|------|--------|
| 1 | Read [**docs/IMPLEMENTATION.md**](./docs/IMPLEMENTATION.md) for **current vs next** and ecosystem boundaries. |
| 2 | Complete remaining pre-implementation items in the [plan checklist (section 9)](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md#9-pre-implementation-checklist-tasks-before-coding) (e.g. Elasticsearch license for your org; Debezium spike rolls into Stage C / D1). |
| 3 | Execute [backlog section 10](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md#10-implementation-backlog-for-the-coding-agent--execute-after-9) from row **5** onward. |
| 4 | Target **~26–49 person-days** for the full demo vertical slice ([plan section 7](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md#7-effort-and-complexity-estimated)). |

**Prerequisites:** JDK **21**, **Docker** + Compose for infra scripts and integration tests, **≥ 16 GB RAM** recommended when Kafka/Connect are added (see **SEARCH FEATURE.md** local demo scope).

## Demo and implementation scope

| Scope | Description |
|-------|-------------|
| **Reference demo (when code exists)** | **Docker** images and **Docker Compose** for local run and optional CI smoke—see **SEARCH FEATURE.md** section 19 |
| **Production-style runtime (proposed)** | **Kubernetes** (any CNCF distro: **AKS**, **GKE**, etc.)—see section 20 |
| **Managed cloud** | Optional mapping tables for **Azure** and **GCP** in section 21 |

## CDC decision (summary)

| Aspect | Choice |
|--------|--------|
| **Publishing from inventory** | **Transactional outbox** — events written in the **same database transaction** as business mutations (no app-level dual write to Kafka). |
| **Capture to Kafka** | **Debezium** on **Kafka Connect** (log-based CDC from the OLTP WAL/binlog). |
| **Escape hatch** | Vendor or managed CDC **into the same Kafka topics** only if mandated; see **SEARCH FEATURE.md** section 6.5. |

## Architecture defaults (summary)

Aligned with common **cloud-native** practice; full rationale is in **SEARCH FEATURE.md** section 16.

| Area | Default |
|------|---------|
| **OLTP reference** | **PostgreSQL** for v1 (Debezium logical decoding); other engines use the same outbox + topic contract via section 6.5 if required. |
| **Hosting** | **Managed-first** Kafka, Connect (Debezium plugin), and Elasticsearch/OpenSearch where TCO allows; section 21 lists Azure/GCP names. |
| **Multi-tenant search** | Shared cluster + **mandatory `tenant_id`** on documents and queries. |
| **Freshness** | Target **p99 pipeline ≤ ~5s** under normal load (tunable with ES refresh and bulk tuning). |
| **Data lake** | **Phase 1b** unless audit/regulation requires immediate topic→**S3/HDFS**/Blob/GCS mirror. |

## Observability (summary)

Industry-standard **OpenTelemetry** instrumentation in Spring Boot 3 (**Micrometer Observation** + OTLP), **OpenTelemetry Collector**, **Prometheus**-compatible metrics (including **JMX** for Kafka Connect/Debezium), **Grafana** (or cloud APM) for dashboards and SLOs, **Kibana/OpenSearch Dashboards** for Elasticsearch-native diagnostics, structured logs correlated with **trace_id**. **Azure Monitor / Application Insights** and **Google Cloud Observability** are listed as OTLP/managed backends in sections 18 and 21. See **SEARCH FEATURE.md** section 18 (mitigation catalog, monitoring checklist, starter SLOs).

## Documentation

| Document | Purpose |
|----------|---------|
| [**docs/IMPLEMENTATION.md**](./docs/IMPLEMENTATION.md) | **You are here:** current step (post-A / pre-B), next steps, first-draft ecosystem vs architecture, ports, validation |
| [**SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md**](./SEARCH_DEMO_AND_README_IMPLEMENTATION_PLAN.md) | Validated layers, demo v1 decisions, person-day estimates, checklist, backlog with **done / next** markers |
| [**SEARCH FEATURE.md**](./SEARCH%20FEATURE.md) | Full architecture: industry alignment, defaults, observability, Docker/K8s/cloud, CDC, refresh, topics |

## Industry alignment (summary)

Common **open-source** cores: **Kafka**, **Elasticsearch or OpenSearch**, **Debezium**, **transactional outbox**, **OpenTelemetry**, optional **object store + Spark/Flink** for lake-scale history. **Docker Compose** for demo; **Kubernetes** + managed services for scale.

The design matches **CQRS-style** product/catalog systems (OLTP for writes, search index for reads) described in industry write-ups—**PostgreSQL → CDC/outbox → Kafka → Elasticsearch** is a well-documented pattern; see **SEARCH FEATURE.md** sections 3.4–3.5 for **quality attributes** (freshness, eventual consistency, operability) and **anti-patterns avoided** (dual write, silent pipeline stall, treating ES as source of truth).

## Planned stack (summary)

- **Runtime (repo):** Java **21**, Spring Boot **3.4**; Spring Cloud (Gateway, optional Config/Discovery) **planned**
- **Local demo:** **Dockerfile** per service + **`docker-compose.yml`** (Postgres, Kafka, Connect+Debezium, ES, Redis, apps)—section 19
- **Search:** Elasticsearch (+ Kibana) or OpenSearch; Spring Data Elasticsearch or official ES Java API client
- **Events / CDC:** **Transactional outbox** in **inventory-api-service**; **Debezium** (Kafka Connect) → Kafka; unwrap/SMT or relay; **Spring Kafka** consumers (enrichment, indexer)
- **Observability:** **OTel** + Micrometer, Collector, Prometheus/Mimir, Grafana, Tempo/Jaeger (or **Azure Monitor** / **GCP** backends—section 21)
- **Batch / lake when needed:** **HDFS** or **cloud object storage** (S3, **Azure Blob**, **GCS**) with **Spark/Flink** for full rebuilds, archives, diff/reconciliation
- **Cache:** Redis (shared) + Caffeine (in-process), with strict cache-key rules for multi-tenant and pricing context

## Testing (planned)

Integration tests with **Testcontainers** (PostgreSQL, Kafka, Elasticsearch; **Debezium** / Connect where CI budget allows), plus **contract tests** on **unwrapped** outbox event schemas so indexer tests do not depend on raw Debezium envelopes. Optional **Compose** smoke job (section 19).

## Repository layout

| Path | Purpose |
|------|---------|
| `shared-contracts/` | JSON Schemas + fixture-based contract tests |
| `inventory-api-service/` | Port **8080** — inventory API (outbox in Stage C) |
| `search-query-service/` | Port **8081** — search API (Elasticsearch in Stage B) |
| `search-indexer-service/` | Port **8082** — Kafka → ES indexer (Stage C) |
| `compose/docker-compose.infra.yml` | D0: Postgres, Elasticsearch, Redis |
| `docs/IMPLEMENTATION.md` | Current step, next steps, ecosystem map |
| `scripts/onboard.sh` | Developer onboarding + `mvnw verify` |
| `scripts/validate-infra.sh` | Health checks when Compose D0 is running |

A full **`docker-compose.yml`** (Kafka + Connect + Debezium + apps) and optional **`k8s/`** remain per **SEARCH FEATURE.md** sections 19–20.

## License

See [LICENSE](./LICENSE).
