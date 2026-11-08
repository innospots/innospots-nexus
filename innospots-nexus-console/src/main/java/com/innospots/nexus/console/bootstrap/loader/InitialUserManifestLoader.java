package com.innospots.nexus.console.bootstrap.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.bootstrap.parser.InitialUserManifestParser;

/**
 * 从 classpath 加载并合并全部初始用户清单。
 *
 * <p>与角色清单一致，使用 {@link ClassLoader#getResources(String)} 枚举多 jar 中的
 * 同名 {@code console/bootstrap/users.yaml}。</p>
 *
 * <p><b>合并策略：</b>以 {@code loginName}（大小写不敏感）为键；任一文件声明
 * {@code enabled: false} 即整体关闭用户初始化。同一登录名在多个文件中出现且定义
 * 不一致时启动失败，避免静默覆盖。</p>
 */
public final class InitialUserManifestLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialUserManifestLoader.class);

    /** 清单固定路径；多个 jar 中的同名文件会被全部加载。 */
    public static final String RESOURCE_PATH = "console/bootstrap/users.yaml";

    private final InitialUserManifestParser parser;
    private final ClassLoader classLoader;

    /**
     * 创建加载器。
     *
     * @param parser      清单解析器
     * @param classLoader 资源类加载器；为空时使用线程上下文类加载器
     */
    public InitialUserManifestLoader(
            InitialUserManifestParser parser,
            ClassLoader classLoader
    ) {
        if (parser == null) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "InitialUserManifestParser is required");
        }
        this.parser = parser;
        ClassLoader actual = classLoader == null
                ? Thread.currentThread().getContextClassLoader()
                : classLoader;
        if (actual == null) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Initial user manifest classLoader is required");
        }
        this.classLoader = actual;
    }

    /**
     * 合并后的初始用户清单。
     *
     * @param enabled 是否执行初始化
     * @param users   初始用户
     */
    public record LoadedInitialUsers(boolean enabled, List<InitialUser> users) {
    }

    /**
     * 加载初始用户清单。
     *
     * @return 加载结果；清单不存在时返回 {@code enabled=true} 与空列表
     */
    public LoadedInitialUsers load() {
        InputStream inputStream = classLoader.getResourceAsStream(RESOURCE_PATH);
        if (inputStream == null) {
            LOGGER.info("No initial user manifest found at {}, skip", RESOURCE_PATH);
            return new LoadedInitialUsers(true, List.of());
        }
        InitialUserManifest manifest = parser.parse(read(inputStream), RESOURCE_PATH);
        Map<String, InitialUser> distinct = new LinkedHashMap<>();
        for (InitialUser user : manifest.users()) {
            if (user == null || isBlank(user.loginName())) {
                throw ConsoleExceptions.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Initial user without loginName in " + RESOURCE_PATH);
            }
            String key = user.loginName().trim().toLowerCase(Locale.ROOT);
            if (distinct.putIfAbsent(key, user) != null) {
                throw ConsoleExceptions.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Duplicated loginName in " + RESOURCE_PATH + ": " + user.loginName());
            }
        }
        LOGGER.info(
                "Loaded initial user manifest from {}, {} user(s), enabled={}",
                RESOURCE_PATH,
                distinct.size(),
                manifest.isEnabled());
        return new LoadedInitialUsers(manifest.isEnabled(), List.copyOf(distinct.values()));
    }

    private static String normalizedRealm(InitialUser user) {
        return isBlank(user.securityRealm())
                ? "PLATFORM"
                : user.securityRealm().trim().toUpperCase(Locale.ROOT);
    }

    private static String read(InputStream inputStream) {
        try (InputStream stream = inputStream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot read initial user manifest: " + RESOURCE_PATH,
                    exception);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
