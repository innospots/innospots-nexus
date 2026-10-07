#!/usr/bin/env bash
#
# 在目标服务器初始化本服务的 releases / shared 目录（部署用户执行一次即可）。
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# shellcheck source=service.env
source "${SCRIPT_DIR}/service.env"

mkdir -p "${DEPLOY_APP_BASE}/releases"
mkdir -p "${DEPLOY_APP_BASE}/shared/config"
mkdir -p "${DEPLOY_APP_BASE}/shared/logs"

echo "Initialized ${DEPLOY_APP_BASE}"
echo "Place production config under: ${DEPLOY_APP_BASE}/shared/config/"
