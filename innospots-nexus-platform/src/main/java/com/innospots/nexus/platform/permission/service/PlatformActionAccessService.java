package com.innospots.nexus.platform.permission.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.jaxrs.support.ConsolePublicApiPaths;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsoleResourcePermissionAuthorizer;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.domain.ownership.ConsoleOwnershipScope;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantDescriptor;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantPathMatcher;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.platform.permission.domain.request.ActionAccessItem;
import com.innospots.nexus.platform.permission.domain.request.ActionAccessRequest;
import com.innospots.nexus.platform.permission.domain.vo.ActionAccessVo;

/**
 * 按已注册端点授权规则校验当前平台用户的页面操作。
 *
 * @author Smars
 * @date 2026/11/04
 * @see EndpointGrantManager
 */
public final class PlatformActionAccessService {
    private final EndpointGrantManager manager;
    private final ConsoleResourcePermissionAuthorizer authorizer;
    private final AuthorizationSubjectResolver subjects;
    private final PermissionVisibilityService visibility;
    private final ConsoleCatalogService catalog;

    /**
     * 创建依赖接口目录、主体与授权服务的校验器。
     *
     * @param manager 注册的接口目录
     * @param authorizer 与请求过滤器共享的授权器
     * @param subjects 当前会话主体解析器
     * @param visibility 当前主体授权键查询服务
     * @param catalog 权限目录
     */
    public PlatformActionAccessService(
            EndpointGrantManager manager,
            ConsoleResourcePermissionAuthorizer authorizer,
            AuthorizationSubjectResolver subjects,
            PermissionVisibilityService visibility,
            ConsoleCatalogService catalog
    ) {
        this.manager = manager;
        this.authorizer = authorizer;
        this.subjects = subjects;
        this.visibility = visibility;
        this.catalog = catalog;
    }

    /**
     * 返回各请求的授权结果及当前页面模块的授权组。
     *
     * @param request 页面归属和待检查请求
     * @return 不可变授权结果
     */
    public ActionAccessVo verify(ActionAccessRequest request) {
        AuthorizationSubject subject = subjects.resolve().orElse(null);
        Map<String, Boolean> decisions = new LinkedHashMap<>();
        List<EndpointGrantDescriptor> routes = manager.allModuleCatalogs().values().stream()
                .flatMap(module -> java.util.stream.Stream.concat(module.readOnly().stream(),
                        module.operable().stream()))
                .distinct().toList();
        if (request != null && request.requests() != null && request.requests().size() <= 256) {
            for (ActionAccessItem item : request.requests()) {
                if (item == null || item.method() == null || item.url() == null) {
                    continue;
                }
                String key = item.method() + ":" + item.url();
                decisions.computeIfAbsent(key, ignored -> allowed(item, subject, routes));
            }
        }
        Set<String> keys = subject == null ? Set.of()
                : visibility.grantedGrantKeys(ConsoleOwnershipScope.grantQueryOwnership(null), subject);
        String domain = request == null || request.domain() == null ? "" : request.domain().trim();
        String module = request == null || request.module() == null ? "" : request.module().trim();
        List<String> groups = catalog.permissionCatalog().stream()
                .filter(item -> domain.equals(item.domainKey()))
                .flatMap(item -> item.modules().stream())
                .filter(item -> module.equals(item.moduleKey()) && item.grants() != null)
                .flatMap(item -> java.util.stream.Stream.concat(item.grants().viewGroups().stream(),
                        item.grants().operationGroups().stream()))
                .filter(group -> keys.contains("*") || keys.contains(group.resourceKey()))
                .map(CatalogPermissionGroupVo::group)
                .filter(group -> group != null && !group.isBlank())
                .distinct().sorted().toList();
        return new ActionAccessVo(decisions, groups);
    }

    /** Verify that a page grant group is present in the current catalog. */
    public boolean hasPageGrant(String domain, String module, String pageKey, String group) {
        if (domain == null || module == null || pageKey == null || group == null || domain.isBlank()
                || module.isBlank() || pageKey.isBlank() || group.isBlank()) {
            return false;
        }
        return catalog.permissionCatalog().stream()
                .filter(item -> domain.equals(item.domainKey()))
                .flatMap(item -> item.modules().stream())
                .filter(item -> module.equals(item.moduleKey()) && pageKey.equals(item.entryPageKey())
                        && item.grants() != null)
                .flatMap(item -> java.util.stream.Stream.concat(item.grants().viewGroups().stream(),
                        item.grants().operationGroups().stream()))
                .anyMatch(item -> group.equals(item.group()));
    }

    private boolean allowed(
            ActionAccessItem item,
            AuthorizationSubject subject,
            List<EndpointGrantDescriptor> routes
    ) {
        String method = item.method().trim().toUpperCase(java.util.Locale.ROOT);
        String path = item.url().split("\\?", 2)[0];
        if (path.length() > 2048 || !path.startsWith("/api/") || path.contains("..") || path.contains("#")
                || path.contains("%") || path.contains(";") || path.contains("\\") || path.contains("//")) {
            return false;
        }
        for (String part : path.split("/")) {
            if (part.contains("${") && !part.matches("\\$\\{[a-zA-Z_][a-zA-Z0-9_.]*}")) {
                return false;
            }
        }
        String normalized = path.replaceAll("\\$\\{[a-zA-Z_][a-zA-Z0-9_.]*}", "_parameter_");
        if (normalized.contains("$") || normalized.contains("{")) {
            return false;
        }
        String targetPath = path;
        List<EndpointGrantDescriptor> candidates = routes.stream()
                .filter(candidate -> candidate.httpMethod().equals(method)
                        && couldMatch(candidate.path(), targetPath))
                .toList();
        if (candidates.size() != 1) {
            return false;
        }
        EndpointGrantDescriptor grant = candidates.getFirst();
        if (ConsolePublicApiPaths.matches(path) || (grant.ignoreAuth() && grant.ignorePermission())) {
            return true;
        }
        if (subject == null) {
            return false;
        }
        if (grant.ignorePermission()) {
            return true;
        }
        ResolvedEndpointGrant resolved = new ResolvedEndpointGrant(grant.moduleKey(), grant.domainKey(),
                grant.grantGroup(), grant.action(), grant.name(), null, grant.recordArgs(), grant.auditLog(),
                grant.ignorePermission(), grant.ignoreAuth());
        return authorizer.authorizeEndpoint(null, resolved, subject).allowed();
    }

    private static boolean couldMatch(String template, String path) {
        if (!path.contains("${")) {
            return EndpointGrantPathMatcher.matches(template, path);
        }
        String[] templateParts = template.split("/");
        String[] pathParts = path.split("/");
        if (templateParts.length != pathParts.length) {
            return false;
        }
        for (int i = 0; i < templateParts.length; i++) {
            boolean unknown = pathParts[i].startsWith("${") && pathParts[i].endsWith("}");
            boolean variable = templateParts[i].startsWith("{") && templateParts[i].endsWith("}");
            if (!unknown && !variable && !templateParts[i].equals(pathParts[i])) {
                return false;
            }
        }
        return true;
    }

}
