package com.innospots.nexus.console.bootstrap.parser;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RoleAuthorizationManifestParser} YAML 解析测试。
 */
class RoleAuthorizationManifestParserTest {

    private final RoleAuthorizationManifestParser parser = new RoleAuthorizationManifestParser();

    @Test
    void parsesFullManifest() {
        RoleAuthorizationManifest manifest = parser.parse("""
                roles:
                  - roleCode: sys_admin
                    roleName: 系统管理员
                    nameEn: System Administrator
                    description: 平台内置系统管理员
                    ownerType: PLATFORM
                    administrator: true
                    sortOrder: 10
                    grants:
                      - domain: system
                        module: logger
                        grantGroups: [view]
                      - domain: operation
                        module: role
                        grantGroups: [create]
                """, "test");

        assertThat(manifest.roles()).hasSize(1);
        RoleDefinition role = manifest.roles().get(0);
        assertThat(role.roleCode()).isEqualTo("sys_admin");
        assertThat(role.roleName()).isEqualTo("系统管理员");
        assertThat(role.nameEn()).isEqualTo("System Administrator");
        assertThat(role.description()).isEqualTo("平台内置系统管理员");
        assertThat(role.ownerType()).isEqualTo("PLATFORM");
        assertThat(role.admin()).isTrue();
        assertThat(role.order()).isEqualTo(10);
        assertThat(role.grants()).hasSize(2);
        assertThat(role.grants().get(1).domain()).isEqualTo("operation");
        assertThat(role.grants().get(1).module()).isEqualTo("role");
        assertThat(role.grants().get(1).grantGroups()).containsExactly("create");
    }

    @Test
    void defaultsAdministratorToFalseAndOrderToOne() {
        RoleAuthorizationManifest manifest = parser.parse("""
                roles:
                  - roleCode: viewer
                    roleName: 只读
                    ownerType: PLATFORM
                """, "test");

        RoleDefinition role = manifest.roles().get(0);
        assertThat(role.admin()).isFalse();
        assertThat(role.order()).isEqualTo(1);
        assertThat(role.grants()).isNull();
    }

    @Test
    void returnsEmptyManifestForBlankDocument() {
        RoleAuthorizationManifest manifest = parser.parse("", "test");

        assertThat(manifest.roles()).isEmpty();
    }

    @Test
    void failsOnMalformedYaml() {
        assertThatThrownBy(() -> parser.parse("roles: [unclosed", "bad.yaml"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("bad.yaml");
    }

    @Test
    void failsOnUnknownField() {
        assertThatThrownBy(() -> parser.parse("""
                roles:
                  - roleCode: viewer
                    roleName: 只读
                    ownerType: PLATFORM
                    roleNames: 拼错字段
                """, "bad.yaml"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("bad.yaml");
    }
}
