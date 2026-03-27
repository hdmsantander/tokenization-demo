# Local infrastructure (Compose)

Part of the **first-draft ecosystem**; see [**docs/IMPLEMENTATION.md**](../docs/IMPLEMENTATION.md) for **current vs next** implementation steps and [**docs/CONFIGURATION.md**](../docs/CONFIGURATION.md) for **profiles** (`localhost` vs `docker` hostnames) and env overrides.

## `docker-compose.infra.yml` (D0)

PostgreSQL 16 (**`wal_level=logical`** for future Debezium), Elasticsearch 8.12, Redis 7. Used for read-path development (**Stage B** next) and OLTP prep before Kafka is introduced (**Stage D1**).

```bash
# From repository root
docker compose -f compose/docker-compose.infra.yml up -d
./scripts/validate-infra.sh
docker compose -f compose/docker-compose.infra.yml down -v
```

### Ports

| Service        | Host port |
|----------------|-----------|
| PostgreSQL     | 5432      |
| Elasticsearch  | 9200      |
| Redis          | 6379      |

### Resources

Elasticsearch is capped at **512 MB** heap in Compose. For a comfortable laptop dev loop, **16 GB RAM** total for host is recommended when later adding Kafka and Connect.

### Not included yet

Full pipeline (**Kafka**, **Kafka Connect**, **Debezium**) will ship in **`docker-compose.yml`** (Stage D1). This file intentionally stays small for fast iteration.
