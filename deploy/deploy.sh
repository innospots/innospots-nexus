#!/usr/bin/env bash
#
# Release 部署：解压 tar.gz → 挂载 shared config/logs → 切换 current → startup
#
# 用法:
#   ./deploy/deploy.sh <slug>              # 见 deploy/apps.env
#   ./deploy/deploy.sh --all               # 部署清单中的全部服务
#
# 环境变量:
#   INNOSPOTS_DEPLOY_BASE   默认 /opt/innospots
#   GITHUB_SHA              Release 目录名（默认: 时间戳）
#   DEPLOY_DIST_DIR         tar.gz 目录（默认: <repo>/innospots-nexus-sample/dist）
#   DEPLOY_KEEP_RELEASES    保留版本数（默认 5）
#   DEPLOY_HEALTH_RETRIES   健康检查次数（默认 30）
#   DEPLOY_HEALTH_INTERVAL  健康检查间隔秒（默认 2）
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
APPS_ENV="${SCRIPT_DIR}/apps.env"

INNOSPOTS_DEPLOY_BASE="${INNOSPOTS_DEPLOY_BASE:-/opt/innospots}"
DEPLOY_DIST_DIR="${DEPLOY_DIST_DIR:-${REPO_ROOT}/innospots-nexus-sample/dist}"
DEPLOY_KEEP_RELEASES="${DEPLOY_KEEP_RELEASES:-5}"
DEPLOY_HEALTH_RETRIES="${DEPLOY_HEALTH_RETRIES:-30}"
DEPLOY_HEALTH_INTERVAL="${DEPLOY_HEALTH_INTERVAL:-2}"
RELEASE_ID="${GITHUB_SHA:-$(date +%Y%m%d-%H%M%S)}"

usage() {
    echo "Usage: $0 <slug|--all|--list>"
    echo "Apps (from ${APPS_ENV}):"
    while IFS= read -r line || [[ -n "${line}" ]]; do
        [[ "${line}" =~ ^[[:space:]]*# ]] && continue
        [[ -z "${line// }" ]] && continue
        echo "  $(echo "${line}" | awk '{print $1}')"
    done < "${APPS_ENV}"
}

lookup_app() {
    local slug="$1"
    while IFS= read -r line || [[ -n "${line}" ]]; do
        [[ "${line}" =~ ^[[:space:]]*# ]] && continue
        [[ -z "${line// }" ]] && continue
        if [[ "$(echo "${line}" | awk '{print $1}')" == "${slug}" ]]; then
            PACKAGE_NAME="$(echo "${line}" | awk '{print $2}')"
            HEALTH_URL="$(echo "${line}" | awk '{print $3}')"
            STARTUP_PROFILE="$(echo "${line}" | awk '{print $4}')"
            return 0
        fi
    done < "${APPS_ENV}"
    return 1
}

extract_release() {
    local package_path="$1"
    local release_dir="$2"
    local package_name="$3"
    local stage_dir="${release_dir}.staging.$$"

    rm -rf "${stage_dir}"
    mkdir -p "${stage_dir}"
    # 发行包带一级根目录 <artifactId>/；在 staging 内展平后再原子替换 release_dir
    tar -xzf "${package_path}" -C "${stage_dir}"
    local inner="${stage_dir}/${package_name}"
    if [[ -d "${inner}" ]]; then
        shopt -s dotglob
        mv "${inner}"/* "${stage_dir}/"
        rmdir "${inner}"
        shopt -u dotglob
    fi
    if [[ -e "${release_dir}" ]]; then
        mv "${release_dir}" "${release_dir}.old.$(date +%s)"
    fi
    mv "${stage_dir}" "${release_dir}"
}

link_shared_dirs() {
    local release_dir="$1"
    local base_dir="$2"

    mkdir -p "${base_dir}/shared/config" "${base_dir}/shared/logs"

    rm -rf "${release_dir}/config"
    ln -s "${base_dir}/shared/config" "${release_dir}/config"

    rm -rf "${release_dir}/logs"
    ln -s "${base_dir}/shared/logs" "${release_dir}/logs"
}

wait_for_health() {
    local url="$1"
    local i

    echo "Waiting for health: ${url}"
    for i in $(seq 1 "${DEPLOY_HEALTH_RETRIES}"); do
        if curl --fail --silent --max-time 5 "${url}" > /dev/null 2>&1; then
            echo "Health check passed (${i}/${DEPLOY_HEALTH_RETRIES})."
            return 0
        fi
        echo "Waiting... ${i}/${DEPLOY_HEALTH_RETRIES}"
        sleep "${DEPLOY_HEALTH_INTERVAL}"
    done
    return 1
}

rollback() {
    local base_dir="$1"
    local failed_release="$2"
    local old_release="$3"
    local profile="$4"

    echo "Rolling back to: ${old_release}"

    if [[ -x "${failed_release}/bin/shutdown.sh" ]]; then
        "${failed_release}/bin/shutdown.sh" "${profile}" || true
        sleep 3
    fi

    if [[ -n "${old_release}" && -d "${old_release}" ]]; then
        ln -sfn "${old_release}" "${base_dir}/current"
        cd "${base_dir}/current"
        env -u RUNNER_TRACKING_ID ./bin/startup.sh "${profile}"
    else
        echo "No previous release to roll back to."
        return 1
    fi
}

prune_releases() {
    local base_dir="$1"
    local keep="$2"

    if [[ ! -d "${base_dir}/releases" ]]; then
        return 0
    fi
    cd "${base_dir}/releases"
    ls -1dt */ 2>/dev/null | tail -n +"$((keep + 1))" | xargs -r rm -rf
}

deploy_one() {
    local slug="$1"

    if ! lookup_app "${slug}"; then
        echo "ERROR: Unknown app slug: ${slug}"
        usage
        exit 1
    fi

    if [[ -n "${DEPLOY_HEALTH_URL:-}" ]]; then
        HEALTH_URL="${DEPLOY_HEALTH_URL}"
    fi
    if [[ -n "${DEPLOY_STARTUP_PROFILE:-}" ]]; then
        STARTUP_PROFILE="${DEPLOY_STARTUP_PROFILE}"
    fi

    local base_dir="${DEPLOY_APP_BASE:-${INNOSPOTS_DEPLOY_BASE}/${slug}}"
    local release_dir="${base_dir}/releases/${RELEASE_ID}"
    local package_path="${DEPLOY_DIST_DIR}/${PACKAGE_NAME}.tar.gz"
    local old_release=""

    echo "================================="
    echo "Deploy ${slug}"
    echo "Package: ${package_path}"
    echo "Release: ${RELEASE_ID}"
    echo "Base:    ${base_dir}"
    echo "Profile: ${STARTUP_PROFILE}"
    echo "================================="

    if [[ ! -f "${package_path}" ]]; then
        echo "ERROR: Package not found: ${package_path}"
        echo "Build first: mvn -pl innospots-nexus-sample -am package -DskipTests"
        exit 1
    fi

    mkdir -p "${base_dir}/releases"

    if [[ -L "${base_dir}/current" ]]; then
        old_release="$(cd "${base_dir}/current" && pwd -P)"
    fi

    extract_release "${package_path}" "${release_dir}" "${PACKAGE_NAME}"
    chmod +x "${release_dir}/bin/"*.sh
    link_shared_dirs "${release_dir}" "${base_dir}"

    if [[ -n "${old_release}" && -x "${old_release}/bin/shutdown.sh" ]]; then
        echo "Stopping current release: ${old_release}"
        "${old_release}/bin/shutdown.sh" "${STARTUP_PROFILE}" || true
        sleep 3
    fi

    ln -sfn "${release_dir}" "${base_dir}/current"

    cd "${base_dir}/current"
    echo "Starting new release..."
    env -u RUNNER_TRACKING_ID ./bin/startup.sh "${STARTUP_PROFILE}"

    if ! wait_for_health "${HEALTH_URL}"; then
        echo "ERROR: Health check failed for ${slug}."
        rollback "${base_dir}" "${release_dir}" "${old_release}" "${STARTUP_PROFILE}" || exit 1
        exit 1
    fi

    prune_releases "${base_dir}" "${DEPLOY_KEEP_RELEASES}"
    echo "Deploy ${slug} finished. current -> ${release_dir}"
}

main() {
    if [[ ! -f "${APPS_ENV}" ]]; then
        echo "ERROR: Missing ${APPS_ENV}"
        exit 1
    fi

    if [[ $# -lt 1 ]]; then
        usage
        exit 1
    fi

    case "$1" in
        --list)
            while IFS= read -r line || [[ -n "${line}" ]]; do
                [[ "${line}" =~ ^[[:space:]]*# ]] && continue
                [[ -z "${line// }" ]] && continue
                echo "$(echo "${line}" | awk '{print $1}')"
            done < "${APPS_ENV}"
            ;;
        --all)
            while IFS= read -r line || [[ -n "${line}" ]]; do
                [[ "${line}" =~ ^[[:space:]]*# ]] && continue
                [[ -z "${line// }" ]] && continue
                slug="$(echo "${line}" | awk '{print $1}')"
                deploy_one "${slug}"
            done < "${APPS_ENV}"
            ;;
        -h|--help)
            usage
            ;;
        *)
            deploy_one "$1"
            ;;
    esac
}

main "$@"
