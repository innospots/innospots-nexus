package com.innospots.nexus.console.bootstrap.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.parser.RoleAuthorizationManifestParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link RoleAuthorizationManifestLoader} 单资源加载测试。
 *
 * <p>配置只由服务端提供一份，固定在 {@code console/bootstrap/roles.yaml}，
 * 不跨模块或插件 jar 合并。</p>
 */
class RoleAuthorizationManifestLoaderTest {

    private final RoleAuthorizationManifestParser parser = new RoleAuthorizationManifestParser();

    @Test
    void returnsEmptyWhenManifestMissing() throws IOException {
        try (URLClassLoader loader = isolatedClassLoader()) {
            assertThat(new RoleAuthorizationManifestLoader(parser, loader).loadRoles()).isEmpty();
        }
    }

    @Test
    void loadsSingleManifest(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                roles:
                  - roleCode: auditor
                    roleName: 审计员
                    ownerType: PLATFORM
                    grants:
                      - domain: system
                        module: logger
                        grantGroups: [view]
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            List<RoleDefinition> roles = new RoleAuthorizationManifestLoader(parser, loader).loadRoles();

            assertThat(roles).hasSize(1);
            RoleDefinition role = roles.get(0);
            assertThat(role.roleCode()).isEqualTo("auditor");
            assertThat(role.roleName()).isEqualTo("审计员");
            assertThat(role.ownerType()).isEqualTo("PLATFORM");
            assertThat(role.grants()).hasSize(1);
            assertThat(role.grants().get(0).domain()).isEqualTo("system");
            assertThat(role.grants().get(0).module()).isEqualTo("logger");
            assertThat(role.grants().get(0).grantGroups()).containsExactly("view");
        }
    }

    @Test
    void expandsMultipleGroupsOfOneModule(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                roles:
                  - roleCode: sys_admin
                    roleName: 系统管理员
                    ownerType: PLATFORM
                    grants:
                      - domain: system
                        module: settings
                        grantGroups: [view, operate, update]
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            List<RoleDefinition> roles = new RoleAuthorizationManifestLoader(parser, loader).loadRoles();

            assertThat(roles).hasSize(1);
            assertThat(roles.get(0).grants()).hasSize(1);
            assertThat(RoleAuthorizationManifestLoader.expandGrantKeys(roles.get(0).grants()))
                    .containsExactly(
                            "system.settings:view",
                            "system.settings:operate",
                            "system.settings:update");
        }
    }

    @Test
    void normalizesRoleDefaults(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                roles:
                  - roleCode: viewer
                    ownerType: platform
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            RoleDefinition role = new RoleAuthorizationManifestLoader(parser, loader).loadRoles().get(0);

            // 未声明 roleName / nameEn 时回退 roleCode；ownerType 统一大写。
            assertThat(role.roleName()).isEqualTo("viewer");
            assertThat(role.nameEn()).isEqualTo("viewer");
            assertThat(role.ownerType()).isEqualTo("PLATFORM");
            assertThat(role.admin()).isFalse();
            assertThat(role.order()).isEqualTo(1);
        }
    }

    @Test
    void rejectsDuplicatedRoleCode(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                roles:
                  - roleCode: auditor
                    roleName: 审计员
                    ownerType: PLATFORM
                  - roleCode: auditor
                    roleName: 审计专家
                    ownerType: PLATFORM
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            RoleAuthorizationManifestLoader manifestLoader =
                    new RoleAuthorizationManifestLoader(parser, loader);

            assertThatThrownBy(manifestLoader::loadRoles)
                    .isInstanceOf(NexusException.class)
                    .hasMessageContaining("auditor");
        }
    }

    @Test
    void rejectsRoleWithoutRoleCode(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                roles:
                  - roleName: 无编码角色
                    ownerType: PLATFORM
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            RoleAuthorizationManifestLoader manifestLoader =
                    new RoleAuthorizationManifestLoader(parser, loader);

            assertThatThrownBy(manifestLoader::loadRoles)
                    .isInstanceOf(NexusException.class)
                    .hasMessageContaining("roleCode");
        }
    }

    /**
     * 在临时目录下按 {@code console/bootstrap/roles.yaml} 写入清单。
     *
     * @return 该目录，用作类加载器根
     */
    private Path writeManifest(Path root, String content) throws IOException {
        Path dir = root.resolve("console").resolve("bootstrap");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("roles.yaml"), content.stripIndent(), StandardCharsets.UTF_8);
        return root;
    }

    /**
     * 构造只暴露本测试目录的类加载器，避免宿主机 classpath 上的真实清单干扰断言。
     */
    private URLClassLoader isolatedClassLoader(Path... roots) {
        URL[] urls = new URL[roots.length];
        for (int i = 0; i < roots.length; i++) {
            try {
                urls[i] = roots[i].toUri().toURL();
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }
        return new URLClassLoader(urls, RoleAuthorizationManifestLoaderTest.class.getClassLoader()) {
            @Override
            public URL getResource(String name) {
                if (!RoleAuthorizationManifestLoader.RESOURCE_PATH.equals(name)) {
                    return super.getResource(name);
                }
                return resolveFromRoots(name);
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if (!RoleAuthorizationManifestLoader.RESOURCE_PATH.equals(name)) {
                    return super.getResources(name);
                }
                URL resource = resolveFromRoots(name);
                return resource == null
                        ? java.util.Collections.emptyEnumeration()
                        : java.util.Collections.enumeration(List.of(resource));
            }

            private URL resolveFromRoots(String name) {
                for (URL url : urls) {
                    try {
                        URL resource = new URL(url, name);
                        if ("file".equals(resource.getProtocol())
                                && Files.isRegularFile(Path.of(resource.toURI()))) {
                            return resource;
                        }
                    } catch (Exception ignored) {
                        // 忽略非法 URL，继续尝试下一个根
                    }
                }
                return null;
            }
        };
    }
}
