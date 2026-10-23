package com.innospots.nexus.core.endpoint.grant.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

/**
 * 从 JAX-RS 资源类（含实现的 console 契约接口）解析 HTTP 动词与路径模板。
 */
public final class EndpointGrantHttpIntrospection {

    private EndpointGrantHttpIntrospection() {
    }

    /**
     * 解析资源类上某公开实例方法暴露的 HTTP 路由。
     *
     * @param resourceClass JAX-RS 根资源类
     * @param method        资源方法
     * @return 零条或多条绑定（通常一条）
     */
    public static List<HttpRouteBinding> resolveRoutes(Class<?> resourceClass, Method method) {
        if (resourceClass == null || method == null) {
            return List.of();
        }
        Method declared = findMethod(resourceClass, method);
        if (declared == null) {
            return List.of();
        }
        Set<String> httpMethods = resolveHttpMethods(resourceClass, declared);
        if (httpMethods.isEmpty()) {
            return List.of();
        }
        String classPath = resolveClassPath(resourceClass);
        String methodPath = resolveMethodPath(resourceClass, declared);
        String fullPath = joinPaths(classPath, methodPath);
        List<HttpRouteBinding> bindings = new ArrayList<>();
        for (String httpMethod : httpMethods) {
            bindings.add(new HttpRouteBinding(httpMethod, fullPath));
        }
        return bindings;
    }

    private static Method findMethod(Class<?> type, Method method) {
        try {
            return type.getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static String resolveClassPath(Class<?> resourceClass) {
        Path path = resourceClass.getAnnotation(Path.class);
        if (path != null) {
            return path.value();
        }
        return "";
    }

    private static String resolveMethodPath(Class<?> resourceClass, Method method) {
        Path direct = method.getAnnotation(Path.class);
        if (direct != null) {
            return direct.value();
        }
        for (Class<?> iface : resourceClass.getInterfaces()) {
            Method ifaceMethod = findMethod(iface, method);
            if (ifaceMethod == null) {
                continue;
            }
            Path onInterface = ifaceMethod.getAnnotation(Path.class);
            if (onInterface != null) {
                return onInterface.value();
            }
        }
        return "";
    }

    private static Set<String> resolveHttpMethods(Class<?> resourceClass, Method method) {
        Set<String> verbs = readHttpVerbs(method);
        if (!verbs.isEmpty()) {
            return verbs;
        }
        for (Class<?> iface : resourceClass.getInterfaces()) {
            Method ifaceMethod = findMethod(iface, method);
            if (ifaceMethod == null) {
                continue;
            }
            verbs = readHttpVerbs(ifaceMethod);
            if (!verbs.isEmpty()) {
                return verbs;
            }
        }
        return Set.of();
    }

    private static Set<String> readHttpVerbs(Method method) {
        Set<String> verbs = new LinkedHashSet<>();
        addIfPresent(verbs, method, GET.class, "GET");
        addIfPresent(verbs, method, POST.class, "POST");
        addIfPresent(verbs, method, PUT.class, "PUT");
        addIfPresent(verbs, method, DELETE.class, "DELETE");
        addIfPresent(verbs, method, PATCH.class, "PATCH");
        addIfPresent(verbs, method, HEAD.class, "HEAD");
        addIfPresent(verbs, method, OPTIONS.class, "OPTIONS");
        return verbs;
    }

    private static void addIfPresent(
            Set<String> verbs,
            Method method,
            Class<? extends Annotation> annotationType,
            String name
    ) {
        if (method.getAnnotation(annotationType) != null) {
            verbs.add(name);
        }
    }

    static String joinPaths(String base, String relative) {
        String normalizedBase = normalizeSegment(base);
        String normalizedRelative = normalizeSegment(relative);
        if (normalizedBase.isEmpty()) {
            return normalizedRelative.isEmpty() ? "/" : normalizedRelative;
        }
        if (normalizedRelative.isEmpty()) {
            return normalizedBase;
        }
        if (normalizedBase.endsWith("/") && normalizedRelative.startsWith("/")) {
            return normalizedBase + normalizedRelative.substring(1);
        }
        if (!normalizedBase.endsWith("/") && !normalizedRelative.startsWith("/")) {
            return normalizedBase + "/" + normalizedRelative;
        }
        return normalizedBase + normalizedRelative;
    }

    private static String normalizeSegment(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("/")) {
            trimmed = "/" + trimmed;
        }
        while (trimmed.length() > 1 && trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * HTTP 动词与路径模板绑定。
     *
     * @param httpMethod    大写 HTTP 方法
     * @param pathTemplate  合并路径模板
     */
    public record HttpRouteBinding(String httpMethod, String pathTemplate) {
        public HttpRouteBinding {
            httpMethod = httpMethod == null ? "" : httpMethod;
            pathTemplate = pathTemplate == null ? "" : pathTemplate;
        }
    }
}
