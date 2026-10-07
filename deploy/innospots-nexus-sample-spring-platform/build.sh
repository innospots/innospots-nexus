#!/usr/bin/env bash
#
# 仅构建 innospots-nexus-sample-spring-platform 发行包（tar.gz）。
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# shellcheck source=service.env
source "${SCRIPT_DIR}/service.env"

cd "${REPO_ROOT}"
mvn -B -pl "${MVN_MODULE}" -am clean package -DskipTests

DIST="${DEPLOY_DIST_DIR:-${REPO_ROOT}/innospots-nexus-sample/dist}"
ls -lh "${DIST}/${PACKAGE_NAME}.tar.gz"
