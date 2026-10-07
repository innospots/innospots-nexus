#!/usr/bin/env bash
#
# 在目标 Linux 服务器上一次性初始化目录（以 github-runner 或部署用户执行）。
# 示例: INNOSPOTS_DEPLOY_BASE=/opt/innospots ./deploy/server-init.sh
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INNOSPOTS_DEPLOY_BASE="${INNOSPOTS_DEPLOY_BASE:-/opt/innospots}"

while IFS= read -r line || [[ -n "${line}" ]]; do
    [[ "${line}" =~ ^[[:space:]]*# ]] && continue
    [[ -z "${line// }" ]] && continue
    slug="$(echo "${line}" | awk '{print $1}')"
    base="${INNOSPOTS_DEPLOY_BASE}/${slug}"
    mkdir -p "${base}/releases"
    mkdir -p "${base}/shared/config"
    mkdir -p "${base}/shared/logs"
    echo "Initialized ${base}"
done < "${SCRIPT_DIR}/apps.env"

echo "Done. Place production files under each .../shared/config/"
