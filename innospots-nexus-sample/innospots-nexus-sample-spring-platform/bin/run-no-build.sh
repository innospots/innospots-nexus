#!/usr/bin/env bash
#
# Start the sample Spring platform from existing target/classes.
# This script never compiles sources or packages a JAR.
#
# Usage:
#   bin/run-no-build.sh
#   bin/run-no-build.sh --spring.profiles.active=test --server.port=18082
#
# Environment:
#   JAVA_HOME                    JDK 25+ home
#   NEXUS_CLASSPATH_FILE        Pre-generated dependency classpath file
#   NEXUS_REFRESH_CLASSPATH=1   Regenerate dependency classpath before start
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${MODULE_DIR}/../.." && pwd)"
TARGET_DIR="${MODULE_DIR}/target"
MAIN_CLASS="com.innospots.nexus.sample.spring.platform.SamplePlatformConsoleServer"

project_class_dirs=(
    "${TARGET_DIR}/classes"
    "${REPO_ROOT}/innospots-nexus-spring/innospots-nexus-spring-platform/target/classes"
    "${REPO_ROOT}/innospots-nexus-spring/innospots-nexus-spring-console/target/classes"
    "${REPO_ROOT}/innospots-nexus-spring/innospots-nexus-spring-core/target/classes"
    "${REPO_ROOT}/innospots-nexus-platform/target/classes"
    "${REPO_ROOT}/innospots-nexus-console/target/classes"
    "${REPO_ROOT}/innospots-nexus-plugin/target/classes"
    "${REPO_ROOT}/innospots-nexus-core/target/classes"
    "${REPO_ROOT}/innospots-nexus-base/target/classes"
)

missing_dirs=()
for dir in "${project_class_dirs[@]}"; do
    if [[ ! -d "${dir}" ]]; then
        missing_dirs+=("${dir#${REPO_ROOT}/}")
    fi
done
if (( ${#missing_dirs[@]} > 0 )); then
    printf 'Missing compiled classes:\n' >&2
    printf '  %s\n' "${missing_dirs[@]}" >&2
    printf 'Build the project in IntelliJ once (Cmd+F9) or run Maven compile once.\n' >&2
    exit 1
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
    JAVA_BIN="${JAVA_HOME}/bin/java"
else
    JAVA_BIN="$(command -v java || true)"
fi
if [[ -z "${JAVA_BIN}" || ! -x "${JAVA_BIN}" ]]; then
    printf 'Java executable not found. Set JAVA_HOME to JDK 25+.\n' >&2
    exit 1
fi

java_version="$("${JAVA_BIN}" -XshowSettings:properties -version 2>&1 \
    | sed -n 's/^[[:space:]]*java.specification.version = //p' \
    | head -n 1)"
if [[ ! "${java_version}" =~ ^[0-9]+$ ]] || (( java_version < 25 )); then
    printf 'JDK 25+ is required, current specification version: %s\n' "${java_version:-unknown}" >&2
    exit 1
fi

classpath_file="${NEXUS_CLASSPATH_FILE:-${TARGET_DIR}/run-runtime-classpath.txt}"
pom_newer_than_classpath=false
if [[ -s "${classpath_file}" ]] \
    && [[ -n "$(find "${REPO_ROOT}" -maxdepth 4 -name pom.xml -type f -newer "${classpath_file}" -print -quit)" ]]; then
    pom_newer_than_classpath=true
fi

if [[ "${NEXUS_REFRESH_CLASSPATH:-0}" == "1" ]] \
    || [[ ! -s "${classpath_file}" ]] \
    || [[ "${pom_newer_than_classpath}" == "true" ]]; then
    mkdir -p "$(dirname "${classpath_file}")"
    tmp_classpath_file="$(mktemp "${classpath_file}.XXXXXX")"
    printf 'Refreshing runtime dependency classpath (no compile/package)...\n'
    if ! mvn -q \
        -f "${REPO_ROOT}/pom.xml" \
        -pl :innospots-nexus-sample-spring-platform \
        -DincludeScope=runtime \
        -Dmdep.outputFile="${tmp_classpath_file}" \
        dependency:build-classpath; then
        rm -f "${tmp_classpath_file}"
        printf 'Failed to resolve the runtime classpath.\n' >&2
        exit 1
    fi
    mv "${tmp_classpath_file}" "${classpath_file}"
fi

runtime_classpath=""
for dir in "${project_class_dirs[@]}"; do
    runtime_classpath+="${dir}:"
done
runtime_classpath+="$(<"${classpath_file}")"

printf 'Starting %s from existing classes (no build).\n' "${MAIN_CLASS}"
cd "${REPO_ROOT}"
exec "${JAVA_BIN}" \
    -XX:TieredStopAtLevel=1 \
    -Dspring.output.ansi.enabled=always \
    -Dfile.encoding=UTF-8 \
    -Dsun.stdout.encoding=UTF-8 \
    -Dsun.stderr.encoding=UTF-8 \
    -cp "${runtime_classpath}" \
    "${MAIN_CLASS}" \
    "$@"
