package com.innospots.nexus.console.bootstrap.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.ModuleGrant;
import com.innospots.nexus.console.bootstrap.domain.config.RoleAuthorizationManifest.RoleDefinition;
import com.innospots.nexus.console.bootstrap.parser.RoleAuthorizationManifestParser;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 从服务端 classpath 加载角色授权清单。
 *
 * <p><b>单一来源：</b>配置只由服务端提供一份，固定在
 * {@value #RESOURCE_PATH}，不跨模块或插件 jar 合并。清单缺失视为未配置，
 * 返回空列表以便未启用初始化的部署正常启动。</p>
 *
 * <p><b>校验：</b>同一文件内 {@code roleCode} 不允许重复，重复即视为配置错误，
 * 避免后者静默覆盖前者。</p>
 *
 * @author Smars
 * @date 2026/11/08
 */
public final class RoleAuthorizationManifestLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAuthorizationManifestLoader.class);

    /** 清单固定路径。 */
    public static final String RESOURCE_PATH = "console/bootstrap/roles.yaml";

    private final RoleAuthorizationManifestParser parser;
    private final ClassLoader classLoader;

    /**
     * 创建加载器。
     *
     * @param parser      清单解析器
     * @param classLoader 资源类加载器；为空时使用线程上下文类加载器
     */
    public RoleAuthorizationManifestLoader(
            RoleAuthorizationManifestParser parser,
            ClassLoader classLoader
    ) {
        if (parser == null) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "RoleAuthorizationManifestParser is required");
        }
        this.parser = parser;
        ClassLoader actual = classLoader == null
                ? Thread.currentThread().getContextClassLoader()
                : classLoader;
        if (actual == null) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Role authorization manifest classLoader is required");
        }
        this.classLoader = actual;
    }

    /**
     * 加载角色授权清单。
     *
     * @return 角色定义列表，按声明顺序排列；清单不存在时返回空列表
     */
    public List<RoleDefinition> loadRoles() {
        InputStream inputStream = classLoader.getResourceAsStream(RESOURCE_PATH);
        if (inputStream == null) {
            LOGGER.info("No role authorization manifest found at {}, skip", RESOURCE_PATH);
            return List.of();
        }
        String content = read(inputStream);
        RoleAuthorizationManifest manifest = parser.parse(content, RESOURCE_PATH);
        Map<String, RoleDefinition> distinct = new LinkedHashMap<>();
        for (RoleDefinition definition : manifest.roles()) {
            if (definition == null || isBlank(definition.roleCode())) {
                throw ConsoleExceptions.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Role definition without roleCode in " + RESOURCE_PATH);
            }
            String roleCode = definition.roleCode().trim();
            RoleDefinition normalized = normalize(definition, roleCode);
            if (distinct.putIfAbsent(roleCode, normalized) != null) {
                throw ConsoleExceptions.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Duplicated roleCode in " + RESOURCE_PATH + ": " + roleCode);
            }
        }
        LOGGER.info(
                "Loaded role authorization manifest from {}, {} role(s)",
                RESOURCE_PATH,
                distinct.size());
        return List.copyOf(distinct.values());
    }

    private static String read(InputStream inputStream) {
        try (InputStream stream = inputStream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot read role authorization manifest: " + RESOURCE_PATH,
                    exception);
        }
    }

    private static RoleDefinition normalize(RoleDefinition definition, String roleCode) {
        String roleName = isBlank(definition.roleName()) ? roleCode : definition.roleName().trim();
        String ownerType = isBlank(definition.ownerType())
                ? "PLATFORM"
                : definition.ownerType().trim().toUpperCase(Locale.ROOT);
        List<ModuleGrant> grants = definition.grants() == null ? List.of() : definition.grants();
        return new RoleDefinition(
                roleCode,
                roleName,
                isBlank(definition.nameEn()) ? roleName : definition.nameEn().trim(),
                definition.description(),
                ownerType,
                definition.administrator(),
                definition.sortOrder(),
                grants);
    }

    /**
     * 展开为落库授权键列表：{@code domain.module:grantGroup}。
     *
     * @param grants 模块授权列表
     * @return 授权键列表，按声明顺序展开
     */
    public static List<String> expandGrantKeys(List<ModuleGrant> grants) {
        if (grants == null || grants.isEmpty()) {
            return List.of();
        }
        List<String> keys = new ArrayList<>();
        for (ModuleGrant grant : grants) {
            if (grant == null || isBlank(grant.domain()) || isBlank(grant.module())) {
                continue;
            }
            String prefix = grant.domain().trim() + "." + grant.module().trim() + ":";
            for (String group : grant.normalizedGroups()) {
                keys.add(prefix + group);
            }
        }
        return keys;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
