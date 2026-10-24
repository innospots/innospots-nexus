package com.innospots.nexus.core.endpoint.grant;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.ws.rs.container.ResourceInfo;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantAnnotations;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantHttpIntrospection;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantPathMatcher;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.core.endpoint.grant.I18nLiteralSupport;

/**
 * 汇总已注册 JAX-RS 资源的 {@link EndpointGrant} 元数据：按模块输出目录 VO，并支持请求匹配与 {@link ResourceInfo} 解析。
 *
 * <p>宿主在启动时对 Spring 中声明了 {@link EndpointModule} 的资源 Bean 调用
 * {@link #registerResource(Class)}（含 platform/portal 实现类及其 console 契约接口上的模块/grant）。</p>
 */
public final class EndpointGrantManager {

    private final Map<String, ModuleAccumulator> modules = new ConcurrentHashMap<>();
    private final List<RouteEntry> routes = new ArrayList<>();

    /**
     * 扫描并注册资源类上的全部 grant 方法。
     *
     * @param resourceClass JAX-RS 根资源类
     */
    public synchronized void registerResource(Class<?> resourceClass) {
        if (resourceClass == null) {
            return;
        }
        for (Method method : resourceClass.getMethods()) {
            if (method.getDeclaringClass() == Object.class) {
                continue;
            }
            registerMethod(resourceClass, method);
        }
    }

    /**
     * 按模块键读取聚合目录（只读 / 可操作 / 忽略鉴权 / 忽略授权）。
     *
     * @param moduleKey entry 模块键
     * @return 目录；未知模块时为空
     */
    public Optional<EndpointModuleGrantCatalog> moduleCatalog(String moduleKey) {
        if (moduleKey == null || moduleKey.isBlank()) {
            return Optional.empty();
        }
        ModuleAccumulator accumulator = modules.get(moduleKey);
        if (accumulator == null) {
            return Optional.empty();
        }
        return Optional.of(accumulator.toCatalog());
    }

    /**
     * 全部已注册模块的目录快照（键为模块键）。
     *
     * @return 不可变映射
     */
    public Map<String, EndpointModuleGrantCatalog> allModuleCatalogs() {
        Map<String, EndpointModuleGrantCatalog> snapshot = new LinkedHashMap<>();
        for (Map.Entry<String, ModuleAccumulator> entry : modules.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().toCatalog());
        }
        return Map.copyOf(snapshot);
    }

    /**
     * 按 HTTP 方法与请求路径匹配已注册的 grant 描述。
     *
     * @param httpMethod  HTTP 动词
     * @param requestPath 请求路径（Jersey {@code UriInfo#getPath()} 形态）
     * @return 匹配到的描述
     */
    public Optional<EndpointGrantDescriptor> match(String httpMethod, String requestPath) {
        if (httpMethod == null || httpMethod.isBlank()) {
            return Optional.empty();
        }
        String normalizedMethod = httpMethod.trim().toUpperCase();
        for (RouteEntry route : routes) {
            if (!route.httpMethod().equals(normalizedMethod)) {
                continue;
            }
            if (EndpointGrantPathMatcher.matches(route.pathTemplate(), requestPath)) {
                return Optional.of(route.descriptor());
            }
        }
        return Optional.empty();
    }

    /**
     * 从当前请求定位到的资源方法解析 grant 描述（与 {@link EndpointGrantAnnotations} 策略一致）。
     *
     * @param resourceInfo JAX-RS 资源信息
     * @return 描述；未标注 grant 时为 empty
     */
    public Optional<EndpointGrantDescriptor> resolve(ResourceInfo resourceInfo) {
        if (resourceInfo == null) {
            return Optional.empty();
        }
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (method == null || resourceClass == null) {
            return Optional.empty();
        }
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, resourceClass);
        if (grant == null) {
            return Optional.empty();
        }
        List<EndpointGrantHttpIntrospection.HttpRouteBinding> bindings =
                EndpointGrantHttpIntrospection.resolveRoutes(resourceClass, method);
        String path = bindings.isEmpty() ? "" : bindings.get(0).pathTemplate();
        String httpMethod = bindings.isEmpty() ? "" : bindings.get(0).httpMethod();
        return Optional.of(toDescriptor(resourceClass, method, httpMethod, path, grant));
    }

    private void registerMethod(Class<?> resourceClass, Method method) {
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, resourceClass);
        if (grant == null || grant.moduleKey().isBlank()) {
            return;
        }
        List<EndpointGrantHttpIntrospection.HttpRouteBinding> bindings =
                EndpointGrantHttpIntrospection.resolveRoutes(resourceClass, method);
        if (bindings.isEmpty()) {
            return;
        }
        for (EndpointGrantHttpIntrospection.HttpRouteBinding binding : bindings) {
            EndpointGrantDescriptor descriptor = toDescriptor(
                    resourceClass,
                    method,
                    binding.httpMethod(),
                    binding.pathTemplate(),
                    grant);
            routes.add(new RouteEntry(binding.httpMethod(), binding.pathTemplate(), descriptor));
            modules.computeIfAbsent(grant.moduleKey(), ModuleAccumulator::new).add(descriptor);
        }
    }

    private static EndpointGrantDescriptor toDescriptor(
            Class<?> resourceClass,
            Method method,
            String httpMethod,
            String path,
            ResolvedEndpointGrant grant
    ) {
        I18nObject label = I18nLiteralSupport.toI18nObject(grant.label());
        return new EndpointGrantDescriptor(
                grant.moduleKey(),
                grant.domainKey(),
                grant.grantGroup(),
                httpMethod,
                path,
                grant.name(),
                label,
                grant.action(),
                grant.recordArgs(),
                grant.auditLog(),
                grant.ignorePermission(),
                grant.ignoreAuth(),
                resourceClass.getName(),
                method.getName());
    }

    private static final class RouteEntry {
        private final String httpMethod;
        private final String pathTemplate;
        private final EndpointGrantDescriptor descriptor;

        private RouteEntry(String httpMethod, String pathTemplate, EndpointGrantDescriptor descriptor) {
            this.httpMethod = httpMethod;
            this.pathTemplate = pathTemplate;
            this.descriptor = descriptor;
        }

        private String httpMethod() {
            return httpMethod;
        }

        private String pathTemplate() {
            return pathTemplate;
        }

        private EndpointGrantDescriptor descriptor() {
            return descriptor;
        }
    }

    private static final class ModuleAccumulator {
        private final String moduleKey;
        private final List<EndpointGrantDescriptor> readOnly = new ArrayList<>();
        private final List<EndpointGrantDescriptor> operable = new ArrayList<>();
        private final List<EndpointGrantDescriptor> ignoreAuth = new ArrayList<>();
        private final List<EndpointGrantDescriptor> ignorePermission = new ArrayList<>();

        private ModuleAccumulator(String moduleKey) {
            this.moduleKey = moduleKey;
        }

        private void add(EndpointGrantDescriptor descriptor) {
            if (descriptor.ignoreAuth()) {
                ignoreAuth.add(descriptor);
            }
            if (descriptor.ignorePermission()) {
                ignorePermission.add(descriptor);
            }
            if (isReadOnly(descriptor.action())) {
                readOnly.add(descriptor);
            } else {
                operable.add(descriptor);
            }
        }

        private EndpointModuleGrantCatalog toCatalog() {
            return new EndpointModuleGrantCatalog(
                    moduleKey,
                    List.copyOf(readOnly),
                    List.copyOf(operable),
                    List.copyOf(ignoreAuth),
                    List.copyOf(ignorePermission));
        }
    }

    private static boolean isReadOnly(EndpointGrantAction action) {
        return action == EndpointGrantAction.READ || action == EndpointGrantAction.EXPORT;
    }
}
