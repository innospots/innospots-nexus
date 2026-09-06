FROM eclipse-temurin:25-jdk-noble

# Build baseline pinned by innospots-nexus-parent: JDK 25 + Maven 3.9+.
ENV MAVEN_VERSION=3.9.11 \
    MAVEN_HOME=/opt/maven \
    LANG=zh_CN.UTF-8 \
    LC_ALL=zh_CN.UTF-8

# git and curl are required in fully custom images; the zh_CN.UTF-8 locale makes
# the JVM default locale Chinese, which the base module's bilingual status-code
# tests assert against.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl git locales \
    && sed -i 's/# zh_CN.UTF-8 UTF-8/zh_CN.UTF-8 UTF-8/' /etc/locale.gen \
    && locale-gen \
    && rm -rf /var/lib/apt/lists/*

RUN curl -fsSL -o /tmp/maven.tar.gz \
        "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    && mkdir -p "${MAVEN_HOME}" \
    && tar -xzf /tmp/maven.tar.gz -C "${MAVEN_HOME}" --strip-components=1 \
    && rm /tmp/maven.tar.gz \
    && ln -s "${MAVEN_HOME}/bin/mvn" /usr/local/bin/mvn

ENV PATH="${MAVEN_HOME}/bin:${PATH}"

# Cursor checks out the repository into a workspace owned by the agent user; allow
# git operations regardless of the checkout owner.
RUN git config --system --add safe.directory '*'
