#!/usr/bin/env bash
#
# 发布 innospots-nexus-sample-spring-platform：
#   build（可选）→ 解压 release → shared config/logs → 切换 current → startup
#
# 用法:
#   ./deploy.sh              # 使用已有 dist/*.tar.gz
#   ./deploy.sh --build      # 先 mvn package 再部署
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# shellcheck source=service.env
source "${SCRIPT_DIR}/service.env"

export DEPLOY_APP_BASE="${DEPLOY_APP_BASE}"
export DEPLOY_HEALTH_URL="${DEPLOY_HEALTH_URL}"
export DEPLOY_STARTUP_PROFILE="${DEPLOY_STARTUP_PROFILE}"
export DEPLOY_DIST_DIR="${DEPLOY_DIST_DIR:-${REPO_ROOT}/innospots-nexus-sample/dist}"

if [[ "${1:-}" == "--build" ]]; then
    "${SCRIPT_DIR}/build.sh"
    shift
fi

if [[ $# -gt 0 ]]; then
    echo "Usage: $0 [--build]"
    exit 1
fi

chmod +x "${REPO_ROOT}/deploy/deploy.sh"
exec "${REPO_ROOT}/deploy/deploy.sh" "${DEPLOY_APP_SLUG}"
