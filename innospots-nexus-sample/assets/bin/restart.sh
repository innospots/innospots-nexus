#!/usr/bin/env bash

APP_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd | sed 's/\/bin//')
PROFILE="${1:-default}"

"${APP_DIR}/bin/shutdown.sh" "${PROFILE}"
"${APP_DIR}/bin/startup.sh" "${PROFILE}"
