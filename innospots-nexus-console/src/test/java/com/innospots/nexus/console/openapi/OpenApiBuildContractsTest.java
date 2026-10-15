package com.innospots.nexus.console.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.config.ConsoleConstant;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesConsoleOpenApiSpec() throws Exception {
        Path spec = Path.of("target/classes/META-INF/nexus-openapi/innospots-nexus-console.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Console API");
        assertThat(yaml).contains("bearerAuth");
        assertThat(yaml).contains("AuthLoginRequest:");
        assertThat(yaml).contains("description: 统一 API 响应包装");
        assertThat(yaml).contains("description: 分页结果");
        // 角色 / 字典 / 授权 REST 已下沉至 platform
        assertThat(yaml).doesNotContain("operationId: rolePage");
        assertThat(yaml).doesNotContain("operationId: roleBindingPage");
        assertThat(yaml).doesNotContain("operationId: permissionGrantListByRole");
        assertThat(yaml).doesNotContain(ConsoleConstant.apiPath("/roles/{roleId}/permissions"));
    }
}
