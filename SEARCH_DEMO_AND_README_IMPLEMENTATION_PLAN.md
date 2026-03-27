# Search feature demo and README — implementation plan

This document plans **staged, incremental work** to deliver the **local search demo** (Docker Compose, aligned with [SEARCH FEATURE.md](./SEARCH%20FEATURE.md) **§19**) and to evolve the **project README** once implementation exists. It **validates scope and layers**, records **demo v1 decisions** so coding can start, estimates **effort in person-days** (with assumptions), and lists **pre-implementation tasks** for whoever implements next (human or agent).

**Related documents**

| Document | Role |
|----------|------|
| [README.md](./README.md) | Entry point; status, summaries, links |
| [SEARCH FEATURE.md](./SEARCH%20FEATURE.md) | Full architecture; **§15** phases, **§17** relative effort (T-shirt), **§19** Compose scope |

---

## 1. Current repository state

| Aspect | State |
|--------|--------|
| Implementation | **None** — docs and license only |
| Authoritative demo spec | **SEARCH FEATURE.md §19.1–§19.3** |

**Implication:** All layers below are **greenfield**; the demo is the **integration spine** that proves each layer.

---

## 2. Layer validation and plan corrections

### 2.1 Verdict on the five layers

| Layer | Verdict | Notes |
|-------|---------|--------|
| **1 — Foundations** | **Sound** | Correct first step: build, modules, contracts, minimal Boot apps, Testcontainers baseline. |
| **2 — Read path** | **Sound** | Proves ES mapping and query API **before** Kafka complexity; reduces integration risk. |
| **3 — Ingest path** | **Sound** | Correct ordering: outbox in OLTP → Debezium → consumer → ES; matches **§6** and **§17** critical path. |
| **4 — Compose demo** | **Sound but refine deps** | Compose is not only “after C.4”: an **infra-only** Compose (PG + ES + Redis, no Kafka) can ship **after Layer 2** for local query demos; **full** stack Compose remains **after C.4** (see **§2.3**). |
| **5 — README / UX** | **Sound** | Must trail runnable behavior; keep README thin and link **SEARCH FEATURE.md**. |

### 2.2 Issues fixed in this revision

| Issue | Resolution |
|-------|--------------|
| **Compose dependency** stated only as “after C.4+” | Split into **D0 (infra-only)** vs **D1 (full pipeline)** in **§4** and **§8**. |
| **Open decisions** blocked coding | **Frozen defaults for demo v1** in **§3** (document ID, payload format, minimal domain). |
| **Effort section avoided calendar/person time** | **§7** now gives **person-day ranges** and **calendar hints** with explicit assumptions. |
| **No explicit agent/human pre-flight list** | **§9** lists tasks to complete **before** writing production code paths. |

### 2.3 Scope validation (unchanged summary)

**In scope:** Compose per **§19.1** (full demo), minimal E2E inventory → outbox → Debezium → indexer → ES → query, boundaries **§19.2**, README quickstart when code exists, optional CI smoke **§19.3**.

**Out of scope for demo milestone:** HA, mandatory K8s, mandatory full observability stack, lake/Spark, NL/recipe quality at scale (**§15** later phases).

**Scope checklist**

| Check | Result |
|-------|--------|
| Demo stack matches **§19.1** | **Pass** when full Compose includes PG, Kafka, Connect+Debezium, ES, Spring services; Redis optional |
| README does not promise production SLOs | **Pass** if README defers HA/SLA to **SEARCH FEATURE.md** |
| CDC pattern consistency | **Pass** if demo uses **outbox + Debezium** as in **README** + **§6** |

---

## 3. Demo v1 decisions (frozen for development)

These defaults unblock implementation; revisit only if compliance or licensing forces a change.

| Topic | Demo v1 choice | Rationale |
|-------|----------------|-----------|
| **Search engine** | **Elasticsearch** (official Docker image) | Faster path with Spring Data Elasticsearch docs; **OpenSearch** remains a documented swap (**§16.2**). |
| **Payload on Kafka (demo)** | **JSON** on topics; **no Schema Registry** in v1 | Lower moving parts; add Avro + registry in a later increment (**§6.4**). |
| **Document ID** | **`{tenant_id}:{store_id}:{sku}`** (string) | Stable idempotency key; matches multi-tenant + store slice; align `routing` later if needed (**§6.3.3**). |
| **Enrichment** | **Merged into `search-indexer-service`** | Matches **§2** “may be merged with indexer in v1”; separate `enrichment-worker` only if CPU/lag forces it. |
| **Outbox cleanup** | **Processed flag + periodic job** (or delete-after-publish if relay owns idempotency) | **§6.0** requires explicit design; demo favors simple **mark processed** to avoid unbounded growth. |
| **Gateway** | **Optional in v1** | Call **search-query-service** and **inventory-api-service** directly from demo clients; add Spring Cloud Gateway in Phase 1+ polish. |

---

## 4. Stages and incremental work

### Stage A — Layer 1: Foundations (**§15 Phase 0** subset)

| ID | Deliverable | Complexity | Depends on |
|----|-------------|------------|------------|
| A.1 | Parent POM (Java 21, Spring Boot 3.2+), modules per **§12**, CI workflow (`mvn verify`) | M | — |
| A.2 | `shared-contracts`: JSON Schema (or records) for **unwrapped** outbox / `IndexItem` payloads | M | A.1 |
| A.3 | `search-query-service`: Boot app, health, actuator, config profiles | M | A.1 |
| A.4 | `search-indexer-service`: Boot app + Kafka listener stub (no ES yet) | M | A.1, A.2 |
| A.5 | `inventory-api-service`: Boot app + REST stub | S–M | A.1 |
| A.6 | Testcontainers: ES (+ PG when inventory uses JDBC) | M | A.3+ |

**Exit criteria:** `mvn verify` passes on CI without Compose.

---

### Stage B — Layer 2: Elasticsearch read path

| ID | Deliverable | Complexity | Depends on |
|----|-------------|------------|------------|
| B.1 | Index mapping + settings (`tenant_id`, text fields, filters) **§4.1** | M | A.3 |
| B.2 | Seed: test fixture or `_bulk` script for static docs | S | B.1 |
| B.3 | Query API: query string + structured filters → ES DSL | M | B.1 |
| B.4 | Optional: Redis cache + safe cache keys **§9** | M | B.3 |

**Exit criteria:** Query service returns hits for seeded data **without** Kafka.

---

### Stage C — Layer 3: Ingest path (**§15 Phase 1** core)

| ID | Deliverable | Complexity | Depends on |
|----|-------------|------------|------------|
| C.1 | Flyway/Liquibase: `items` (or equivalent) + **outbox** table | M | A.5 |
| C.2 | Inventory API: business write + outbox insert **same transaction** | M | C.1 |
| C.3 | Outbox cleanup: processed marker + scheduled job or relay | M | C.2 |
| C.4 | Debezium: connector JSON, `wal_level=logical`, slot monitoring notes | L | C.1 |
| C.5 | Consumer reads **unwrapped** payload; partition key = `document_id` | M | C.4, A.2 |
| C.6 | Indexer: map event → ES doc; **idempotent** upsert/delete; **DLQ** | L | C.5, B.1 |

**Exit criteria:** Mutation via inventory API becomes **search-visible** end-to-end (accounting for ES `refresh_interval`).

**Parallelization:** C.4 connector tuning can overlap with C.6 **if** topic names and JSON contract are frozen (A.2).

---

### Stage D — Layer 4: Docker Compose (**§19**)

| ID | Deliverable | Complexity | Depends on |
|----|-------------|------------|------------|
| **D0** | **`docker-compose.infra.yml`** or profile: **PG + ES (+ Redis)** for local dev / Stage B demos | S–M | B.1 (for ES) |
| D.1 | **`docker-compose.yml`**: PG, Kafka (KRaft **§19.1**), Connect+Debezium, ES, Redis | M | C.4 |
| D.2 | Multi-stage **Dockerfiles** per Spring service | S | A.* |
| D.3 | `application-docker.yml` / env; Compose DNS names **§19.2** | M | D.1 |
| D.4 | Init: create topics, register connector (script or documented `curl`) | M | D.1 |
| D.5 | Optional: OTel Collector, Prometheus, Grafana **§19.1** | M | D.1 |
| D.6 | Optional: CI Compose smoke **§19.3** | L | D.1–D.4 |

**Exit criteria:** Clone → `docker compose up` → run init steps → mutate inventory → query sees update.

---

### Stage E — Layer 5: README and ancillary docs

| ID | Deliverable | Complexity | Depends on |
|----|-------------|------------|------------|
| E.1 | README **Quickstart** (Docker, RAM/CPU **§19.2**) | S | D1 exit criteria met |
| E.2 | README **Development** pointer to this file + branch conventions | S | E.1 |
| E.3 | README **Status**: demo runnable vs production | S | E.1 |
| E.4 | `compose/README.md`: resources, troubleshooting (connector lag, ES yellow) | S–M | D.1 |

---

## 5. Critical path

**Inventory TX + outbox → Debezium → Kafka consumer → indexer → ES** is the long pole (**Stage C** + **D1** wiring).

**Query-only path** (**A → B → D0**) is a valid **parallel track** for frontend or contract demos.

---

## 6. Contradictions, risks, mitigations

| Topic | Mitigation |
|-------|------------|
| README “feedback before implementation” | README updated to **implementation-ready** stance with link to this plan (**§3** decisions). |
| Compose ≠ production | Call out **§19.2** in README and `compose/README.md`. |
| Debezium / WAL / slot on laptop | Document limits + link **§18.3** checklist in compose README. |
| CI flake on full stack | Prefer **Testcontainers** per PR; Compose smoke **nightly** or manual until stable (**§19.3**). |

---

## 7. Effort and complexity (estimated)

### 7.1 Unit definitions

| Unit | Meaning |
|------|---------|
| **Person-day (pd)** | One engineer, ~6 hours of **focused** implementation/review/debug (not calendar elapsed). |
| **Complexity** | **S** = isolated; **M** = several files/services; **L** = cross-cutting or ops-heavy; **XL** = program-sized (not used for demo scope). |

### 7.2 Assumptions (read before using numbers)

- Engineer is **comfortable** with **Spring Boot 3**, **Kafka**, and **Elasticsearch** basics (has shipped at least one service using each).
- **No** organizational blockers (license approval for Elastic Docker image, internal registry, etc.).
- **Single** environment target first: **Linux + Docker Compose** on a machine with **≥ 16 GB RAM** (see **§19.2**).
- Estimates include **unit + integration tests** for each stage, not a separate QA team.

If the engineer is **new to Debezium or Connect**, add **+3–5 pd** to Stage C. If **Schema Registry** is required from day one, add **+2–4 pd** across A.2, C.5, and D.4.

### 7.3 Effort by stage (person-days)

| Stage | Low (pd) | High (pd) | Dominant risk |
|-------|----------|-----------|----------------|
| **A** Foundations | 4 | 7 | CI + Testcontainers stability |
| **B** Read path | 5 | 9 | Mapping + query DSL edge cases |
| **C** Ingest path | 12 | 22 | Debezium slot, unwrap, idempotent indexer + DLQ |
| **D** Compose (D0 + D1–D4) | 4 | 8 | Image pins, JVM heap, connector bootstrap |
| **E** README / compose docs | 1 | 3 | Keeping docs in sync with scripts |
| **Optional** D.5 observability | 2 | 5 | Wiring scrape targets locally |
| **Optional** D.6 CI Compose smoke | 3 | 6 | Flakes, timeouts |
| **Total (demo vertical slice, no optional)** | **26** | **49** | Stage **C** |
| **Total with optional D.5 + D6** | **31** | **60** | CI + ops surface |

### 7.4 Calendar translation (illustrative only)

| Team shape | Rough calendar (same order of work as stages) |
|------------|-----------------------------------------------|
| **1 engineer full-time** | ~**5.5–10 weeks** wall-clock for **26–49 pd** (using ~5 pd/week effective after meetings and context switching) |
| **2 engineers** (e.g. one on **B+D0**, one on **A+C** after contracts) | Often **~3–6 weeks** to first full E2E, if merge conflicts and integration are managed daily |

These calendars are **not commitments**; they convert pd ranges under typical productivity. Parallel work reduces calendar time but **does not** reduce total pd much.

### 7.5 Comparison to SEARCH FEATURE.md §17

**§17** T-shirt sizes are **ordinal** (good for prioritization). This **§7** table is **cardinal** (pd) for the **demo slice only**. Items in §17 such as **Gateway**, **lake/Spark**, **multi-region**, and **NL/recipe XL** are **out of scope** for the first demo milestone.

---

## 8. Milestones (product view)

1. **M1 — Queryable index:** A + B (+ **D0** optional) — ES + API without Kafka.  
2. **M2 — Live updates:** C — full CDC path to ES.  
3. **M3 — Packaged demo:** D1–D4 — one-command-ish onboarding for new developers.  
4. **M4 — Docs:** E — README + `compose/README.md`.

---

## 9. Pre-implementation checklist (tasks before coding)

Complete these **in order** before opening large implementation PRs:

| # | Task | Owner | Done when |
|---|------|-------|-----------|
| 1 | Confirm **Elasticsearch** Docker image acceptable for your org (license/legal) | Human | Email or ticket reference optional in README |
| 2 | Create **implementation branch** from `main` (or agreed base) | Human / agent | Branch pushed |
| 3 | **Freeze §3 decisions** or document overrides in a short `docs/ADR-000-demo-v1.md` (optional but recommended) | Human | ADR or explicit “no ADR” note in PR |
| 4 | Add **Maven wrapper** (`mvnw`) if missing — reproducible CI | Agent | `./mvnw verify` works clean checkout |
| 5 | Scaffold **empty** multi-module tree (A.1) and **CI** running `verify` on push | Agent | Green CI on skeleton |
| 6 | Commit **topic names** and **JSON samples** for outbox / indexer in `shared-contracts` | Agent | Contract tests parse samples |
| 7 | **Spike** (≤ 1 pd): local Debezium + PG `wal_level=logical` in throwaway compose — validate connector starts | Agent | Spike doc or comment in compose README |

**After row 7:** proceed with **B.1** and **C.1** in parallel if two contributors; otherwise **B** then **C** to minimize risk.

---

## 10. Implementation backlog (for the coding agent — execute after §9)

Present this as the **default execution order** for autonomous implementation (adjust if CI fails).

| Order | Task ID | Description |
|-------|---------|-------------|
| 1 | A.1 | Parent POM, modules, `.gitignore`, GitHub Actions (or CI) `mvn -B verify` |
| 2 | A.2 | Shared DTOs + JSON Schema or validation for indexer input |
| 3 | A.3–A.5 | Three Boot services with health checks |
| 4 | A.6 | Testcontainers for ES (and PG when JDBC lands) |
| 5 | B.1–B.3 | Mapping, seed, query API + tests |
| 6 | D0 | Infra-only Compose for PG+ES (optional but recommended before C) |
| 7 | C.1–C.3 | Schema + transactional outbox + cleanup |
| 8 | C.4–C.6 | Debezium + consumer + indexer + DLQ |
| 9 | D.1–D.4 | Full `docker-compose.yml`, Dockerfiles, init scripts |
| 10 | E.1–E.4 | README quickstart + compose README |

**Stop points for human review:** after **B.3** (query contract), after **C.6** (first E2E), after **D.4** (demo UX).

---

## 11. Document control

| Version | Date | Summary |
|---------|------|---------|
| 1.0 | 2025-03-27 | Initial plan: layers, stages, scope, contradictions, T-shirt-only complexity. |
| 1.1 | 2025-03-27 | Layer validation + Compose D0; frozen demo v1 decisions (**§3**); person-day + calendar **illustrative** estimates (**§7**); pre-implementation checklist + agent backlog (**§9–§10**). |
