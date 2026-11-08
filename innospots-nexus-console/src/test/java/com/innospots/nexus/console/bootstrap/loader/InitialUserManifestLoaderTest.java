package com.innospots.nexus.console.bootstrap.loader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.bootstrap.loader.InitialUserManifestLoader.LoadedInitialUsers;
import com.innospots.nexus.console.bootstrap.parser.InitialUserManifestParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link InitialUserManifestLoader} 单资源加载测试。
 */
class InitialUserManifestLoaderTest {

    private final InitialUserManifestParser parser = new InitialUserManifestParser();

    @Test
    void defaultsToEnabledWithNoUsersWhenManifestMissing() throws IOException {
        try (URLClassLoader loader = isolatedClassLoader()) {
            LoadedInitialUsers users = new InitialUserManifestLoader(parser, loader).load();

            assertThat(users.enabled()).isTrue();
            assertThat(users.users()).isEmpty();
        }
    }

    @Test
    void loadsSingleManifest(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                enabled: true
                users:
                  - loginName: admin
                    displayName: Admin
                    email: admin@demo.com
                    password: Admin12345
                    securityRealm: PLATFORM
                    roleCodes:
                      - sys_admin
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            LoadedInitialUsers loaded = new InitialUserManifestLoader(parser, loader).load();

            assertThat(loaded.enabled()).isTrue();
            assertThat(loaded.users()).hasSize(1);
            InitialUser user = loaded.users().get(0);
            assertThat(user.loginName()).isEqualTo("admin");
            assertThat(user.password()).isEqualTo("Admin12345");
            assertThat(user.securityRealm()).isEqualTo("PLATFORM");
            assertThat(user.roleCodes()).containsExactly("sys_admin");
        }
    }

    @Test
    void honorsDisabledSwitch(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                enabled: false
                users:
                  - loginName: admin
                    password: Admin12345
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            assertThat(new InitialUserManifestLoader(parser, loader).load().enabled()).isFalse();
        }
    }

    @Test
    void rejectsDuplicatedLoginName(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                users:
                  - loginName: admin
                    password: Admin12345
                  - loginName: Admin
                    password: Other12345
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            InitialUserManifestLoader manifestLoader = new InitialUserManifestLoader(parser, loader);

            assertThatThrownBy(manifestLoader::load)
                    .isInstanceOf(NexusException.class)
                    .hasMessageMatching("(?i).*admin.*");
        }
    }

    @Test
    void rejectsUserWithoutLoginName(@TempDir Path root) throws IOException {
        Path base = writeManifest(root, """
                users:
                  - password: Admin12345
                """);
        try (URLClassLoader loader = isolatedClassLoader(base)) {
            InitialUserManifestLoader manifestLoader = new InitialUserManifestLoader(parser, loader);

            assertThatThrownBy(manifestLoader::load)
                    .isInstanceOf(NexusException.class)
                    .hasMessageContaining("loginName");
        }
    }

    private Path writeManifest(Path root, String content) throws IOException {
        Path dir = root.resolve("console").resolve("bootstrap");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("users.yaml"), content.stripIndent(), StandardCharsets.UTF_8);
        return root;
    }

    private URLClassLoader isolatedClassLoader(Path... roots) {
        URL[] urls = new URL[roots.length];
        for (int i = 0; i < roots.length; i++) {
            try {
                urls[i] = roots[i].toUri().toURL();
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }
        return new URLClassLoader(urls, InitialUserManifestLoaderTest.class.getClassLoader()) {
            @Override
            public URL getResource(String name) {
                if (!InitialUserManifestLoader.RESOURCE_PATH.equals(name)) {
                    return super.getResource(name);
                }
                return resolveFromRoots(name);
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if (!InitialUserManifestLoader.RESOURCE_PATH.equals(name)) {
                    return super.getResources(name);
                }
                URL resource = resolveFromRoots(name);
                return resource == null
                        ? Collections.emptyEnumeration()
                        : Collections.enumeration(List.of(resource));
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
