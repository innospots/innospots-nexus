#!/usr/bin/env bash
set -euo pipefail

MAVEN_VERSION="3.9.9"
MAVEN_HOME="/opt/apache-maven-${MAVEN_VERSION}"
JAVA_HOME_PATH="/usr/lib/jvm/java-25-openjdk-amd64"

require_java_25() {
    if command -v java >/dev/null 2>&1 && java -version 2>&1 | grep -q 'version "25'; then
        return 0
    fi
    sudo DEBIAN_FRONTEND=noninteractive apt-get update -qq
    sudo DEBIAN_FRONTEND=noninteractive apt-get install -y -qq \
        ca-certificates \
        curl \
        openjdk-25-jdk
}

require_maven() {
    if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
        curl -fsSL \
            "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
            | sudo tar -xz -C /opt
    fi
    sudo ln -sf "${MAVEN_HOME}/bin/mvn" /usr/local/bin/mvn
}

write_toolchain_profile() {
    local profile="/etc/profile.d/innospots-nexus-toolchain.sh"
    if [ -f "${profile}" ]; then
        return 0
    fi
    sudo tee "${profile}" >/dev/null <<EOF
export JAVA_HOME=${JAVA_HOME_PATH}
export PATH="${MAVEN_HOME}/bin:\$JAVA_HOME/bin:\$PATH"
EOF
}

require_java_25
require_maven
write_toolchain_profile

export JAVA_HOME="${JAVA_HOME_PATH}"
export PATH="${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${PATH}"

cd /workspace
mvn -q validate
