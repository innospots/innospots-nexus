package com.innospots.nexus.console.openapi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

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
        assertThat(yaml).contains(ConsoleConstant.apiPath("/roles"));
        assertThat(yaml).contains("operationId: rolePage");
        assertThat(yaml).contains("bearerAuth");
        assertThat(yaml).contains("AuthLoginRequest:");
        assertThat(yaml).contains("RoleCreateRequest:");
        assertThat(yaml).contains("RoleVo:");
        assertThat(yaml).contains("description: 统一 API 响应包装");
        assertThat(yaml).contains("description: 分页结果");
        boolean rolePageUsesTypedWrapper = yaml.contains("RPageResultRoleVo:")
                || Pattern.compile(
                                "operationId: rolePage[\\s\\S]*?\\$ref: [\"']#/components/schemas/R\\d+[\"']")
                        .matcher(yaml)
                        .find();
        assertThat(rolePageUsesTypedWrapper).isTrue();
        assertThat(yaml).containsPattern("\\$ref: [\"']#/components/schemas/RoleVo[\"']");
    }
}
