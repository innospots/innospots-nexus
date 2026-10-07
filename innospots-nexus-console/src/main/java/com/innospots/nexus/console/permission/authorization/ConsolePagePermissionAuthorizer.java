package com.innospots.nexus.console.permission.authorization;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 控制台 catalog 页面与 datasource 代理的权限判定（框架中立）。
 *
 * <p>根据 {@link AuthorizationRequest} 中的 page key、HTTP 方法与路径，先校验 PAGE 再校验
 * DATASOURCE 授权；与 Bearer 登录态、{@code ConsoleAuthenticationFilter} 等身份认证无关。
 * 不读取原始 Servlet/JAX-RS 请求，由适配层（如 {@code ConsolePagePermissionFilter}）组装入参。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuthorizationRequest
 */
public final class ConsolePagePermissionAuthorizer {

    private final ConsoleCatalogResourceDao resourceDao;
    private final PermissionGrantDao grantDao;

    /**
     * 使用权限目录和授权记录存储创建请求鉴权器。
     */
    public ConsolePagePermissionAuthorizer(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao
    ) {
        this.resourceDao = resourceDao;
        this.grantDao = grantDao;
    }

    /**
     * 对一个已由应用适配器提取的请求执行鉴权判定。
     *
     * <p>判定顺序固定为先校验 PAGE，再根据 method 和 URL 唯一匹配 DATASOURCE，避免接口路径在
     * 页面权限不足时泄露 datasource 授权信息。</p>
     *
     * @param request 标准化请求鉴权数据
     * @return 允许或拒绝结果；允许时包含后续数据访问适配器使用的约束定义
     */
    public AuthorizationDecision authorize(AuthorizationRequest request) {
        if (request == null || request.workspaceId() == null || request.subject() == null
                || blank(request.method()) || blank(request.path()) || blank(request.pageKey())) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        String pageKey = request.pageKey().trim();
        // pageKey 来自请求头，页面权限是所有 datasource 请求的第一道边界。
        ConsoleCatalogResourceEntity page = page(pageKey);
        if (page == null) {
            return AuthorizationDecision.deny("Page is not available");
        }
        if (!request.subject().administrator()
                && !hasGrant(request.workspaceId(), page.getResourceId(), request.subject())) {
            return AuthorizationDecision.deny("Page permission is required");
        }

        // 只有唯一的 method + URL 映射才可继续，避免同一路径对应多个 datasource 时出现歧义。
        List<ConsoleCatalogResourceEntity> matches = datasources(
                pageKey, request.method(), request.path());
        if (matches.size() != 1) {
            return AuthorizationDecision.deny("Datasource is not uniquely registered");
        }
        ConsoleCatalogResourceEntity datasource = matches.getFirst();
        List<PermissionGrantEntity> grants = grants(
                request.workspaceId(), datasource.getResourceId(), request.subject());
        if (!request.subject().administrator() && grants.isEmpty()) {
            return AuthorizationDecision.deny("Datasource permission is required");
        }
        // 约束在此阶段只读取并随上下文传递，具体查询适配由后续数据访问层负责。
        List<String> constraints = grants.stream()
                .map(PermissionGrantEntity::getConstraintDefinition)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .toList();
        return AuthorizationDecision.allow(new AuthorizationContext(
                request.workspaceId(), pageKey, datasource.getDatasourceKey(), constraints));
    }

    private ConsoleCatalogResourceEntity page(String pageKey) {
        List<ConsoleCatalogResourceEntity> pages = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getResourceType,
                                CatalogResourceType.PAGE.name())
                        .eq(ConsoleCatalogResourceEntity::getResourceKey, "page:" + pageKey)
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name()));
        return pages.size() == 1 ? pages.getFirst() : null;
    }

    private List<ConsoleCatalogResourceEntity> datasources(
            String pageKey,
            String method,
            String path
    ) {
        String actualMethod = method.trim().toUpperCase();
        String actualPath = normalizePath(path);
        List<ConsoleCatalogResourceEntity> candidates = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getPageKey, pageKey)
                        .eq(ConsoleCatalogResourceEntity::getResourceType,
                                CatalogResourceType.DATASOURCE.name())
                        .eq(ConsoleCatalogResourceEntity::getRequestMethod, actualMethod)
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name()));
        return candidates.stream()
                .filter(value -> matches(value.getRequestUrl(), actualPath))
                .toList();
    }

    private List<PermissionGrantEntity> grants(
            String workspaceId,
            String resourceId,
            AuthorizationSubject subject
    ) {
        List<PermissionGrantEntity> result = new ArrayList<>();
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership(workspaceId, null);
        if (!subject.roleIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.roleIds())
                            .eq(PermissionGrantEntity::getResourceId, resourceId),
                    ownership)));
        }
        if (!subject.orgUnitIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ORG_UNIT.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.orgUnitIds())
                            .eq(PermissionGrantEntity::getResourceId, resourceId),
                    ownership)));
        }
        return result;
    }

    private boolean hasGrant(String workspaceId, String resourceId, AuthorizationSubject subject) {
        return !grants(workspaceId, resourceId, subject).isEmpty();
    }

    private static boolean matches(String template, String actual) {
        if (template == null) {
            return false;
        }
        String[] expected = template.split("/", -1);
        String[] received = actual.split("/", -1);
        if (expected.length != received.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            String segment = expected[i];
            if (segment.startsWith("{") && segment.endsWith("}")) {
                if (received[i].isBlank()) {
                    return false;
                }
            } else if (!segment.equals(received[i])) {
                return false;
            }
        }
        return true;
    }

    private static String normalizePath(String value) {
        String path = value.trim();
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        int fragment = path.indexOf('#');
        if (fragment >= 0) {
            path = path.substring(0, fragment);
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
