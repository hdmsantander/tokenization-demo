#!/usr/bin/env bash
# Onboard a new developer machine: prerequisites, Maven build, optional infra health checks.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_ok() { echo -e "${GREEN}OK${NC}  $*"; }
log_warn() { echo -e "${YELLOW}WARN${NC} $*"; }
log_err() { echo -e "${RED}ERR${NC}  $*"; }

need_cmd() {
  if command -v "$1" >/dev/null 2>&1; then
    log_ok "found $1"
  else
    log_err "missing required command: $1"
    exit 1
  fi
}

echo "== Grocery search — onboard =="
echo "Repo: $ROOT"

need_cmd java
need_cmd mvn
java -version 2>&1 | head -1

if command -v docker >/dev/null 2>&1; then
  log_ok "found docker"
else
  log_warn "docker not found — skip compose health checks and integration tests that need Docker"
fi

MVN="${ROOT}/mvnw"
if [[ -x "$MVN" ]]; then
  log_ok "using Maven wrapper"
  BUILD_CMD=("$MVN" -B)
else
  log_warn "mvnw not present — using system mvn"
  BUILD_CMD=(mvn -B)
fi

echo "== Build (unit + contract tests; integration tests if Docker available) =="
"${BUILD_CMD[@]}" -q verify

if command -v docker >/dev/null 2>&1; then
  if docker info >/dev/null 2>&1; then
    echo "== Optional: infra compose health (if stack already running) =="
    if docker compose -f compose/docker-compose.infra.yml ps --status running 2>/dev/null | grep -q .; then
      scripts/validate-infra.sh || true
    else
      log_warn "compose stack not running — start with: docker compose -f compose/docker-compose.infra.yml up -d"
    fi
  else
    log_warn "docker daemon not reachable"
  fi
fi

log_ok "Onboarding build complete."
