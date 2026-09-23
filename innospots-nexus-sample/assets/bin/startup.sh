#!/usr/bin/env bash

if [[ -z "$JAVA_HOME" ]]; then
  echo "ERROR: Set JAVA_HOME to a JDK 25+ installation."
  exit 1
fi

if [[ -n "$1" ]]; then
  PROFILE="$1"
else
  PROFILE="default"
fi

export PROFILE

APP_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd | sed 's/\/bin//')
export APP_DIR
export PID_FILE="${APP_DIR}/proc_pid"
export JAVA_HOME
export JAVA="${JAVA_HOME}/bin/java"
export CONFIG_DIR="${APP_DIR}/config"
export LOG_PATH="${APP_DIR}/logs"
export LOG_FILE="${LOG_PATH}/nexus-sample.log"

if [[ -f "${APP_DIR}/bin/setenv.sh" ]]; then
  # shellcheck source=/dev/null
  source "${APP_DIR}/bin/setenv.sh"
fi

if [[ -z "${SERVER_MAIN_CLASS}" ]]; then
  echo "ERROR: SERVER_MAIN_CLASS is not set (bin/setenv.sh)."
  exit 1
fi

CLASSPATH="${APP_DIR}/lib/*:${CONFIG_DIR}"

JAVA_OPT="-Xms256m -Xmx1g"
JAVA_OPT="${JAVA_OPT} -Dspring.profiles.active=${PROFILE}"
JAVA_OPT="${JAVA_OPT} -Dspring.config.additional-location=optional:file:${CONFIG_DIR}/"
if [[ -f "${CONFIG_DIR}/log4j2.xml" ]]; then
  JAVA_OPT="${JAVA_OPT} -Dlogging.config=${CONFIG_DIR}/log4j2.xml"
fi

initLog() {
  if [[ ! -d "${LOG_PATH}" ]]; then
    mkdir -p "${LOG_PATH}"
  fi
  if [[ ! -f "${LOG_FILE}" ]]; then
    touch "${LOG_FILE}"
  fi
}

getPid() {
  TPID=$(pgrep -f "spring.profiles.active=${PROFILE}.*${SERVER_MAIN_CLASS}" 2>/dev/null || true)
  if [[ -z "${TPID}" ]]; then
    TPID=0
  fi
}

startup() {
  getPid
  initLog
  if [[ ${TPID} -ne 0 ]]; then
    echo "${SERVER_MAIN_CLASS} profile=${PROFILE} already running (PID=${TPID})"
    exit 0
  fi
  echo "Starting ${SERVER_MAIN_CLASS} profile=${PROFILE}"
  nohup "${JAVA}" ${JAVA_OPT} -cp "${CLASSPATH}" "${SERVER_MAIN_CLASS}" >> "${LOG_FILE}" 2>&1 &
  echo $! > "${PID_FILE}"
  sleep 2
  getPid
  if [[ ${TPID} -ne 0 ]]; then
    echo "Started (PID=${TPID}), log: ${LOG_FILE}"
    exit 0
  fi
  echo "Start failed, see ${LOG_FILE}"
  exit 1
}

startup
