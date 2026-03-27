#!/usr/bin/env bash
# Validate compose/docker-compose.infra.yml services respond (run after `docker compose ... up -d`).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

COMPOSE_FILE="compose/docker-compose.infra.yml"
FAIL=0

check_http() {
  local name="$1" url="$2"
  if curl -sf --max-time 5 "$url" >/dev/null; then
    echo "OK  $name ($url)"
  else
    echo "ERR $name ($url)"
    FAIL=1
  fi
}

check_redis() {
  if docker compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping 2>/dev/null | grep -q PONG; then
    echo "OK  redis (PING)"
  else
    echo "ERR redis (PING)"
    FAIL=1
  fi
}

check_postgres() {
  if docker compose -f "$COMPOSE_FILE" exec -T postgres pg_isready -U inventory -d inventory >/dev/null 2>&1; then
    echo "OK  postgres (pg_isready)"
  else
    echo "ERR postgres (pg_isready)"
    FAIL=1
  fi
}

if ! docker compose -f "$COMPOSE_FILE" ps --status running 2>/dev/null | grep -q .; then
  echo "No running containers for $COMPOSE_FILE — start with:"
  echo "  docker compose -f $COMPOSE_FILE up -d"
  exit 1
fi

echo "== Validating infra ($COMPOSE_FILE) =="
check_http "elasticsearch" "http://127.0.0.1:9200/_cluster/health"
check_postgres
check_redis

if [[ "$FAIL" -ne 0 ]]; then
  exit 1
fi
echo "All checks passed."
