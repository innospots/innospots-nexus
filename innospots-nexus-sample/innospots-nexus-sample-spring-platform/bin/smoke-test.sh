#!/usr/bin/env bash
#
# Start the already-compiled application with the test profile, wait for it,
# verify health and OpenAPI routes, then stop it.
#
# Usage:
#   bin/smoke-test.sh
#   SMOKE_TEST_PORT=28082 bin/smoke-test.sh
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
PORT="${SMOKE_TEST_PORT:-18082}"
BASE_URL="http://127.0.0.1:${PORT}"
LOG_FILE="${MODULE_DIR}/target/smoke-test-${PORT}.log"
HEALTH_FILE="$(mktemp)"
SPECS_FILE="$(mktemp)"
APP_PID=""

cleanup() {
    rm -f "${HEALTH_FILE}" "${SPECS_FILE}"
    if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
        kill "${APP_PID}" 2>/dev/null || true
        for _ in {1..20}; do
            if ! kill -0 "${APP_PID}" 2>/dev/null; then
                break
            fi
            sleep 0.25
        done
        if kill -0 "${APP_PID}" 2>/dev/null; then
            kill -9 "${APP_PID}" 2>/dev/null || true
        fi
        wait "${APP_PID}" 2>/dev/null || true
    fi
}
trap cleanup EXIT INT TERM

mkdir -p "$(dirname "${LOG_FILE}")"
: > "${LOG_FILE}"

printf 'Starting smoke test on port %s...\n' "${PORT}"
"${SCRIPT_DIR}/run-no-build.sh" \
    --spring.profiles.active=test \
    --server.port="${PORT}" \
    >"${LOG_FILE}" 2>&1 &
APP_PID=$!

ready=false
for _ in {1..60}; do
    if ! kill -0 "${APP_PID}" 2>/dev/null; then
        printf 'Application exited before becoming ready.\n' >&2
        tail -n 120 "${LOG_FILE}" >&2
        exit 1
    fi
    if curl --fail --silent --show-error --max-time 2 \
        "${BASE_URL}/actuator/health" >"${HEALTH_FILE}" 2>/dev/null; then
        ready=true
        break
    fi
    sleep 1
done

if [[ "${ready}" != "true" ]]; then
    printf 'Application did not become healthy within 60 seconds.\n' >&2
    tail -n 120 "${LOG_FILE}" >&2
    exit 1
fi

if ! grep -q '"status":"UP"' "${HEALTH_FILE}"; then
    printf 'Health endpoint did not report UP:\n' >&2
    cat "${HEALTH_FILE}" >&2
    exit 1
fi

if ! grep -q 'Nexus startup pipeline finished' "${LOG_FILE}"; then
    printf 'Nexus startup pipeline did not finish.\n' >&2
    tail -n 120 "${LOG_FILE}" >&2
    exit 1
fi

if ! curl --fail --silent --show-error --max-time 5 \
    "${BASE_URL}/openapi/specs" >"${SPECS_FILE}"; then
    printf 'OpenAPI catalog endpoint failed.\n' >&2
    exit 1
fi

if ! curl --fail --silent --show-error --max-time 5 \
    "${BASE_URL}/openapi/specs/innospots-nexus-platform" >/dev/null; then
    printf 'Platform OpenAPI endpoint failed.\n' >&2
    exit 1
fi

printf 'Smoke test passed: health UP, startup finished, OpenAPI reachable.\n'
printf 'Log: %s\n' "${LOG_FILE}"
