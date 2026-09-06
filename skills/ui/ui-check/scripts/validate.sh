#!/usr/bin/env bash
# Validate Page DSL YAML files against pactor-page-dsl.schema.yaml
#
# Usage:
#   ./validate.sh path/to/page.yaml
#   ./validate.sh --check-filename ui-pages/
#   ./validate.sh --json path/to/page.yaml

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="${SCRIPT_DIR}/.venv"
PYTHON="${VENV_DIR}/bin/python"
REQUIREMENTS="${SCRIPT_DIR}/requirements.txt"
VALIDATOR="${SCRIPT_DIR}/validate-page-dsl.py"

ensure_deps() {
    if [[ -x "${PYTHON}" ]]; then
        return
    fi
    if ! command -v python3 >/dev/null 2>&1; then
        echo "ERROR: python3 is required" >&2
        exit 2
    fi
    python3 -m venv "${VENV_DIR}"
    "${PYTHON}" -m pip install --quiet --upgrade pip
    "${PYTHON}" -m pip install --quiet -r "${REQUIREMENTS}"
}

ensure_deps
exec "${PYTHON}" "${VALIDATOR}" "$@"
