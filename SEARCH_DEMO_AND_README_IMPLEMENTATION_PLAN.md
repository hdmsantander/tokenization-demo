# Search feature demo and README — implementation plan

This document plans **staged, incremental work** to deliver the **local search demo** (Docker Compose, aligned with [SEARCH FEATURE.md](./SEARCH%20FEATURE.md) **§19**) and to evolve the **project README** once implementation exists. It **validates scope**, flags **contradictions or gaps**, and estimates **complexity** using the same **engineering units** as **§17** (T-shirt sizes: S / M / L / XL). **Calendar duration** is intentionally not estimated here: it depends on team size, parallelization, and prior familiarity with Spring Boot, Kafka, and Elasticsearch (see **§17** note on mature-team assumptions).

**Related documents**

| Document | Role |
|----------|------|
| [README.md](./README.md) | Entry point; status, summaries, links |
| [SEARCH FEATURE.md](./SEARCH%20FEATURE.md) | Full architecture; **§15** phases, **§17** effort, **§19** Compose scope |

---

## 1. Current repository state

| Aspect | State |
|--------|--------|
| Implementation | **None** — docs and license only |
| Branch intent | `cursor/search-demo-and-readme-plan-6dea` — planning for demo + README |
| Authoritative demo spec | **SEARCH FEATURE.md §19.1–§19.3** |

**Implication:** All layers below are **greenfield**; the demo is the **integration spine** that proves each layer.

---

## 2. Scope validation

### 2.1 In scope (demo + README)

| Item | Rationale |
|------|-----------|
| **Docker Compose stack** | **§19.1**: PostgreSQL (logical replication), Kafka, Kafka Connect + Debezium, Elasticsearch (or OpenSearch), Redis (optional for query cache), Spring services built from Dockerfiles |
| **Minimal happy-path E2E** | Inventory (or seed) → outbox → Debezium → Kafka → indexer → ES → query API returns expected hits |
| **Documented boundaries** | **§19.2**: single-host, non-HA, resource pins, demo secrets only |
| **README updates** | Replace or supplement “planning only” with **how to run the demo**, prerequisites, and pointers to **SEARCH FEATURE.md** for depth |
| **Optional CI hook** | **§19.3**: optional Compose smoke vs **Testcontainers** as default for automated tests |

### 2.2 Explicitly out of scope for the “demo milestone”

| Item | Where documented |
|------|------------------|
| Production HA, multi-AZ | **§19.2**, **§20** |
| Kubernetes manifests as a **requirement** | **§20** — next step after Compose |
| Full observability stack as **mandatory** in demo | **§19.1** — OTel/Prometheus/Grafana **optional** |
| Lake / Spark / Hadoop | **Phase 1b+** (**§15**) |
| NL/recipe quality at scale | **Phase 2–3** |

### 2.3 Scope checklist (pass/fail)

| Check | Result |
|-------|--------|
| Demo stack matches **§19.1** component list | **Pass** if all required infra + at least **query + indexer + inventory** paths exist; Redis optional |
| README does not promise production SLOs | **Pass** if README defers HA/SLA to **SEARCH FEATURE.md** |
| Single source of truth for CDC pattern | **Pass** if README and demo both reflect **outbox + Debezium** (**README** summary + **§6**) |
| Stakeholder “feedback before implementation” (**README**) | **Process gap** — see **§6.1**: align README wording when implementation starts |

---

## 3. Layered build model

Layers are **bottom-up**: each layer adds dependencies for the next. Work packages can be **parallelized** where indicated.

```
┌─────────────────────────────────────────────────────────┐
│  Layer 5 — README & developer UX (tracks Layers 1–4)   │
├─────────────────────────────────────────────────────────┤
│  Layer 4 — Demo polish: Compose UX, smoke, docs/compose │
├─────────────────────────────────────────────────────────┤
│  Layer 3 — Ingest path: outbox → Debezium → indexer → ES│
├─────────────────────────────────────────────────────────┤
│  Layer 2 — Read path: ES mapping + search-query-service │
├─────────────────────────────────────────────────────────┤
│  Layer 1 — Foundations: repo layout, build, contracts   │
└─────────────────────────────────────────────────────────┘
```

---

## 4. Stages and incremental work

### Stage A — Layer 1: Foundations (**§15 Phase 0** subset)

| Increment | Deliverable | Complexity | Depends on |
|-----------|-------------|------------|------------|
| A.1 | Parent POM (Java 21, Spring Boot 3.2+), module naming per **§12**, `.gitignore` / CI skeleton | **S** | — |
| A.2 | `shared-contracts` (JSON Schema or Avro for outbox / enriched payloads per **§6.8**) | **S** | A.1 |
| A.3 | `search-query-service` empty Boot app: health, actuator, `application-docker.yml` placeholders | **M** | A.1 |
| A.4 | `search-indexer-service` skeleton (listener stub, no ES yet) | **S–M** | A.1, A.2 |
| A.5 | `inventory-api-service` skeleton (REST stub, DB not wired) | **S–M** | A.1 |
| A.6 | **Testcontainers** baseline (ES first; PG as needed) — **§19.3** | **M** | A.3+ |

**Exit criteria:** `mvn verify` (or equivalent) passes; no Compose required yet.

---

### Stage B — Layer 2: Elasticsearch read path (**Phase 0** continuation)

| Increment | Deliverable | Complexity | Depends on |
|-----------|-------------|------------|------------|
| B.1 | Index mapping + settings (analyzers, `tenant_id`) per **§4.1** | **M** | A.3 |
| B.2 | Seed script or test fixture loading **static** docs into ES | **S** | B.1 |
| B.3 | Query API: text + filters → ES DSL; **no Redis required** for first slice | **M** | B.1 |
| B.4 | Optional: Redis cache with **safe multi-tenant keys** (**§9**, **§17**) | **M** | B.3 |

**Exit criteria:** Hitting query service returns results for seeded data **without** Kafka/Debezium.

---

### Stage C — Layer 3: Ingest path (**§15 Phase 1** core)

| Increment | Deliverable | Complexity | Depends on |
|-----------|-------------|------------|------------|
| C.1 | PostgreSQL migrations: business tables + **transactional outbox** (**§6**) | **M** | A.5 |
| C.2 | Inventory API writes **business + outbox** in **one transaction** | **M** | C.1 |
| C.3 | Design and implement **outbox cleanup / processed marker** (**§6.0** operational note) | **M** | C.2 |
| C.4 | Debezium connector config: outbox table → Kafka topic; `wal_level=logical` in Compose | **M–L** | C.1 |
| C.5 | Unwrap path (SMT or relay) + partition key discipline (**§6.3**) | **M** | C.4 |
| C.6 | **enrichment-worker** *or* merged enrichment in **indexer** for v1 (**§2** table note) | **L** | C.5, A.4 |
| C.7 | Indexer: idempotent bulk upsert/delete, **DLQ** topic, version/tombstone handling (**§7.2**) | **L** | C.6, B.1 |

**Exit criteria:** Single item mutation in inventory becomes **search-visible** through the full pipeline (within demo-tuned **refresh_interval**).

**Parallelization:** C.4–C.5 can overlap with C.6 design if topic contracts are fixed (A.2).

---

### Stage D — Layer 4: Docker Compose demo (**§19**)

| Increment | Deliverable | Complexity | Depends on |
|-----------|-------------|------------|------------|
| D.1 | `docker-compose.yml` + pinned images: PG, Kafka (KRaft preferred **§19.1**), Connect+Debezium, ES, Redis | **S–M** | C.4+ |
| D.2 | Multi-stage **Dockerfiles** per Spring service (**§19.1**) | **S** | A.* |
| D.3 | `application-docker.yml` (or env) aligned with Compose DNS names (**§19.2**) | **M** | D.1 |
| D.4 | Init job or documented steps: register connector, create topics | **M** | D.1 |
| D.5 | Optional: OTel Collector + Prometheus + Grafana (**§19.1** optional) | **M** | D.1 |
| D.6 | Optional: **Compose smoke** job in CI (**§19.3**) | **M–L** | D.1–D.4 |

**Exit criteria:** New developer can follow **one** numbered path: clone → Compose up → seed → query → see updates propagate.

---

### Stage E — Layer 5: README and ancillary docs

| Increment | Deliverable | Complexity | Depends on |
|-----------|-------------|------------|------------|
| E.1 | README **Quickstart** (prereqs: Docker, RAM/CPU hints per **§19.2**) | **S** | D.1+ |
| E.2 | README **Architecture** section: keep short; link **SEARCH FEATURE.md** for CDC, observability, cloud (**§21**) | **S** | E.1 |
| E.3 | README **Status** line: distinguish “demo runnable” vs “production ready” | **S** | E.1 |
| E.4 | Optional `compose/README.md`: resource pins, troubleshooting (connector lag, ES yellow) | **S–M** | D.1 |

**Exit criteria:** README is accurate for **current** repo behavior; no duplicate of **SEARCH FEATURE.md**’s full spec.

---

## 5. Critical path (complexity aggregation)

The **longest dependency chain** for “search updates from inventory” matches **§17**:

**Outbox + inventory TX → Debezium + unwrap → indexer + ES** → aggregate **M + (M–L) + M + L** with parallel work on **query service** and **Compose** once contracts exist.

**README** work is **not** on the critical path for the pipeline but should **trail** D.1–D.4 so instructions stay truthful.

---

## 6. Issues, contradictions, and mitigations

### 6.1 Process vs README

| Finding | Mitigation |
|---------|------------|
| **README** states implementation is deferred pending stakeholder feedback | When Stage A starts, add a **short “Implementation in progress”** banner and date, or gate merge to `main` via your org’s review process |

### 6.2 Architecture doc internal tensions (resolved in design)

| Topic | Tension | Resolution in design |
|-------|---------|----------------------|
| Enrichment placement | Separate **enrichment-worker** in diagram vs “may be merged with indexer in v1” **§2** | **Demo v1:** merge into indexer unless load testing forces split |
| Redis | “Optional in demo” **§19.1** vs Phase 0 **Redis tests** **§15** | **Acceptable:** optional runtime in Compose; tests still validate cache path when Redis is enabled |
| Elasticsearch vs OpenSearch | Both allowed **§16.2** | **Pick one** per `docker-compose.yml` for demo; document license/support choice in README |

### 6.3 Demo vs production expectations

| Risk | Mitigation |
|------|------------|
| Readers confuse Compose with production | README + **§19.2** callout: single-host, no HA; link **§20** |
| WAL / replication slot growth on laptop | Document **connector pause** and slot monitoring pointers (**§18.3**) in `compose/README.md` |

### 6.4 Testing strategy tension

| Finding | Mitigation |
|---------|------------|
| **§19.3** prefers Testcontainers for CI; full **PG + Kafka + Connect + ES** in CI is **M–L** **§17** | Start with **split tests**: unit + Testcontainers ES; add Connect E2E nightly or manual until flake budget allows |

### 6.5 Gaps to decide before coding

| Decision | Why it matters |
|----------|----------------|
| Exact **document ID** key (SKU×store×tenant vs other) | Indexer idempotency and routing **§6.3.3** |
| **Schema Registry** in demo or plain JSON for v1 | Unwrap consumer complexity **§6.4** |
| Minimal **inventory** domain model for demo | Drives outbox payload shape **§6.8** |

---

## 7. Complexity summary (no calendar time)

| Area | Typical aggregate complexity | Notes |
|------|------------------------------|-------|
| Foundations + query-only path (Stages A–B) | **M–L** | Dominated by ES mapping and query DSL |
| Full ingest + idempotent indexer (Stage C) | **L** | Retries, DLQ, version discipline |
| Compose demo wiring (Stage D) | **S–M** | Ops tuning; not HA |
| Full CI E2E with Debezium in every PR | **M–L** | Timeouts and flakes |
| README + compose developer notes (Stage E) | **S** | After demo is stable |

**Interpretation:** Delivering a **credible vertical slice** (Stages A–C + minimal D) is **multiple L-class components** on the critical path; **§17** remains the authoritative per-feature breakdown.

---

## 8. Suggested milestone ordering (product view)

1. **Milestone 1 — Queryable index:** Stages A (partial) + B — proves ES and API contract.  
2. **Milestone 2 — Live index updates:** Stage C — proves outbox + Debezium + indexer.  
3. **Milestone 3 — Repeatable demo:** Stage D — proves onboarding without local JVM setup for infra.  
4. **Milestone 4 — Documentation:** Stage E — README and optional `compose/README.md`.

---

## 9. Document control

| Version | Date | Summary |
|---------|------|---------|
| 1.0 | 2025-03-27 | Initial plan: layers, stages, scope validation, contradictions, complexity (engineering units only). |
