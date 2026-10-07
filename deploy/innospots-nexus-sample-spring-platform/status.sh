#!/usr/bin/env bash
#
# 查看当前 release、健康检查与日志路径。
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# shellcheck source=service.env
source "${SCRIPT_DIR}/service.env"

echo "App base: ${DEPLOY_APP_BASE}"
if [[ -L "${DEPLOY_APP_BASE}/current" ]]; then
    echo "current:  $(cd "${DEPLOY_APP_BASE}/current" && pwd -P)"
else
    echo "current:  (not deployed)"
fi

echo "Health:   ${DEPLOY_HEALTH_URL}"
if curl --fail --silent --max-time 5 "${DEPLOY_HEALTH_URL}"; then
    echo ""
else
    echo "Health check failed or service down."
fi

echo "Logs:     ${DEPLOY_APP_BASE}/shared/logs/nexus-sample.log"
