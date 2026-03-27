# Local infrastructure (Compose)

## `docker-compose.infra.yml` (D0)

PostgreSQL 16 (**`wal_level=logical`** for future Debezium), Elasticsearch 8.12, Redis 7. Used for read-path development and OLTP prep before Kafka is introduced.

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
