#!/usr/bin/env bash

APP_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd | sed 's/\/bin//')
export APP_DIR
export PID_FILE="${APP_DIR}/proc_pid"

if [[ -n "$1" ]]; then
  PROFILE="$1"
else
  PROFILE="default"
fi

export PROFILE

if [[ -f "${APP_DIR}/bin/setenv.sh" ]]; then
  # shellcheck source=/dev/null
  source "${APP_DIR}/bin/setenv.sh"
fi

if [[ -z "${SERVER_MAIN_CLASS}" ]]; then
  echo "ERROR: SERVER_MAIN_CLASS is not set (bin/setenv.sh)."
  exit 1
fi

getPid() {
  TPID=$(pgrep -f "spring.profiles.active=${PROFILE}.*${SERVER_MAIN_CLASS}" 2>/dev/null || true)
  if [[ -z "${TPID}" ]]; then
    TPID=0
  fi
}

rmPid() {
  if [[ -f "${PID_FILE}" ]]; then
    rm -f "${PID_FILE}"
  fi
}

getPid
if [[ ${TPID} -eq 0 ]]; then
  echo "${SERVER_MAIN_CLASS} profile=${PROFILE} is not running"
  rmPid
  exit 0
fi

kill "${TPID}" 2>/dev/null || true
for _ in $(seq 1 20); do
  sleep 1
  getPid
  if [[ ${TPID} -eq 0 ]]; then
    rmPid
    echo "Stopped profile=${PROFILE}"
    exit 0
  fi
done

echo "Stop timeout, try: kill -9 ${TPID}"
exit 1
