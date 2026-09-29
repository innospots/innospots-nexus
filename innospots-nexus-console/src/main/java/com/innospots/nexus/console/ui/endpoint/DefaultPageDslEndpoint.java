package com.innospots.nexus.console.ui.endpoint;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.ui.domain.request.PageDslRenderRequest;
import com.innospots.nexus.console.ui.spec.PageDsl;
import com.innospots.nexus.console.ui.spec.filter.PageDslFilterChain;
import com.innospots.nexus.console.ui.spec.filter.PageDslRenderContext;
import com.innospots.nexus.console.ui.spec.loader.PageDslLoader;
/**
 * 从 classpath 加载 Pactor Page DSL 并通过过滤器链渲染；同时作为 Jakarta REST 资源暴露。
 *
 * <p>HTTP：{@code GET}，路径参数见 {@link PageDslRenderRequest}；查询参数整体作为渲染 {@code state} 绑定入参。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Path(ConsoleConstant.PUBLIC_API_PREFIX + "/ui/pages/{domainKey}/{moduleKey}/{pageKey}")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "UiPage", description = "Pactor 页面 DSL 加载与渲染")
public final class DefaultPageDslEndpoint implements PageDslEndpoint {

    private final PageDslLoader loader;
    private final PageDslFilterChain filterChain;

    /**
     * 使用提供的加载器与过滤器链创建端点。
     *
     * @param loader 页面 DSL 加载器
     * @param filterChain 有序渲染时过滤器
     */
    public DefaultPageDslEndpoint(PageDslLoader loader, PageDslFilterChain filterChain) {
        if (loader == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl loader is required");
        }
        this.loader = loader;
        this.filterChain = filterChain == null ? PageDslFilterChain.create() : filterChain;
    }

    @GET
    @Operation(
            operationId = "uiPageDslRender",
            summary = "加载并渲染页面 DSL",
            description = "路径参数定位 YAML；其余查询参数（?key=value）绑定到页面 state，无固定字段名。")
    public R<PageDsl> render(@BeanParam PageDslRenderRequest request, @Context UriInfo uriInfo) {
        Map<String, Object> parameters = queryParameters(uriInfo);
        return R.ok(render(request.domainKey(), request.moduleKey(), request.pageKey(), parameters));
    }

    @Override
    public PageDsl render(String domainKey, String moduleKey, String pageKey, Map<String, Object> parameters) {
        if (!hasText(domainKey) || !hasText(moduleKey) || !hasText(pageKey)) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl domainKey, moduleKey and pageKey are required");
        }
        Map<String, Object> safeParameters = parameters == null ? Collections.emptyMap() : parameters;
        PageDsl document = loader.load(domainKey, moduleKey, pageKey);
        PageDslRenderContext context = PageDslRenderContext.of(moduleKey, pageKey, document, safeParameters);
        return filterChain.process(context);
    }

    private static Map<String, Object> queryParameters(UriInfo uriInfo) {
        if (uriInfo == null) {
            return Collections.emptyMap();
        }
        MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> parameters = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : query.entrySet()) {
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            parameters.put(entry.getKey(), values.get(0));
        }
        return parameters;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
