# Configuration — centralized defaults and pitfalls

This repository uses a small **`shared-config`** Maven module so integration settings (hosts, ports, topic names) stay **consistent** across services and **one place** documents overrides.

---

## 1. Module layout

| Module | Role |
|--------|------|
| **`shared-config`** | JAR on the classpath; contains `config/grocery-defaults.yml` only (no Java beans — avoids half-wired `@ConfigurationProperties` during skeleton phases). |
| **Service `application.yml`** | `spring.config.import: classpath:config/grocery-defaults.yml` then binds Spring-standard keys from `grocery.*` placeholders. |

---

## 2. `grocery-defaults.yml` (authoritative defaults)

**Location:** `shared-config/src/main/resources/config/grocery-defaults.yml`

| Namespace | Purpose |
|-----------|---------|
| `grocery.postgres.*` | Host, port, database, user, password for JDBC (Stage C). |
| `grocery.elasticsearch.uris` | Elasticsearch HTTP base URL(s). |
| `grocery.kafka.bootstrap-servers` | Kafka brokers for clients (Stage C). |
| `grocery.kafka-topics.*` | Topic names aligned with SEARCH FEATURE.md (`inventory.outbox.events`, `search.item.enriched`, `search.index.dlq`). |
| `grocery.redis.*` | Redis host/port for future query cache (Stage B.4). |

**Environment overrides** (optional, for CI or laptops):

| Variable | Maps to |
|----------|---------|
| `GROCERY_POSTGRES_HOST`, `GROCERY_POSTGRES_PORT`, `GROCERY_POSTGRES_DB`, `GROCERY_POSTGRES_USER`, `GROCERY_POSTGRES_PASSWORD` | `grocery.postgres.*` |
| `GROCERY_ELASTICSEARCH_URIS` | `grocery.elasticsearch.uris` |
| `GROCERY_KAFKA_BOOTSTRAP_SERVERS` | `grocery.kafka.bootstrap-servers` |
| `GROCERY_KAFKA_TOPIC_OUTBOX`, `GROCERY_KAFKA_TOPIC_ENRICHED`, `GROCERY_KAFKA_TOPIC_DLQ` | `grocery.kafka-topics.*` |
| `GROCERY_REDIS_HOST`, `GROCERY_REDIS_PORT` | `grocery.redis.*` |

Relaxed binding applies (e.g. `GROCERY_KAFKA_BOOTSTRAP_SERVERS` → `grocery.kafka.bootstrap-servers`).

---

## 3. Profiles

| Profile | When to use | Effect |
|---------|-------------|--------|
| **(default)** | JVM on **host**, Compose publishes ports | `localhost` for Postgres, Elasticsearch, Kafka, Redis. |
| **`docker`** | JVM runs **inside** the same Compose network as infra | Compose **service DNS** (`postgres`, `elasticsearch`, `kafka`, `redis`). |

**Activate:**

```bash
./mvnw -pl search-query-service spring-boot:run -Dspring-boot.run.profiles=docker
```

---

## 4. Pitfalls deliberately avoided

### 4.1 Host JVM vs container hostname

**Problem:** `application-docker.yml` that hard-codes `postgres:5432` fails when you run Spring on the **host** (name `postgres` does not resolve).

**Approach:** One shared file + **`docker` profile** for in-network hostnames; default profile uses **`localhost`**.

### 4.2 Spring Boot Docker Compose auto-configuration

**Problem:** Boot 3.1+ can try to start Compose from the project when `spring.docker.compose` is enabled, which conflicts with “we manage Compose ourselves” and with mixed host/container runs.

**Approach:** `spring.docker.compose.enabled: false` in **`grocery-defaults.yml`**. The **`docker` profile** means “use Compose **network** names”, not “start Compose for me”.

### 4.3 DataSource / Kafka health before dependencies exist

**Problem:** Declaring `spring.datasource.url` without JPA/Flyway (or pointing health to Kafka) causes **red** actuator health or startup failures during skeleton phases.

**Approach:** **No** JDBC starter on `inventory-api-service` until Stage C. **No** `spring-kafka` on `search-indexer-service` until Stage C. `spring.elasticsearch.uris` and `spring.kafka.bootstrap-servers` are **harmless strings** until the respective starters create clients.

### 4.4 Drifting topic names

**Approach:** Canonical names live under `grocery.kafka-topics` in **`grocery-defaults.yml`**; producers/consumers should reference `${grocery.kafka-topics.*}` when implemented.

---

## 5. Validation

- **Automated:** `search-query-service` tests `SharedConfigIntegrationTest` and `SharedConfigDockerProfileTest` assert resolved `grocery.*` values and `spring.docker.compose.enabled=false`.
- **`shared-config`:** `GroceryDefaultsResourceTest` asserts `grocery-defaults.yml` is packaged.
- **`inventory-api-service`:** `PostgresInfraIntegrationTest` uses Testcontainers; **`org.postgresql:postgresql` (test scope)** is required because Testcontainers does not ship the JDBC driver.
- **Manual:** After `docker compose -f compose/docker-compose.infra.yml up -d`, run a service with `-Dspring-boot.run.profiles=docker` and confirm logs show expected URIs (no connection required for skeleton).

---

## 6. Related documents

| Document | Role |
|----------|------|
| [IMPLEMENTATION.md](./IMPLEMENTATION.md) | Current delivery step and ecosystem |
| [compose/README.md](../compose/README.md) | D0 Compose ports |
| [SEARCH FEATURE.md](../SEARCH%20FEATURE.md) | Architecture (topics, CDC, observability) |

---

## 7. Document control

| Version | Date | Summary |
|---------|------|---------|
| 1.0 | 2025-03-27 | Initial: `shared-config`, `grocery.*`, profiles, env overrides, pitfalls, tests. |
