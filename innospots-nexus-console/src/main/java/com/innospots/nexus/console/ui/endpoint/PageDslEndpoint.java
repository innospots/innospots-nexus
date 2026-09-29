package com.innospots.nexus.console.ui.endpoint;

import com.innospots.nexus.console.ui.spec.PageDsl;

import java.util.Map;

/**
 * 加载与准备页面 DSL 文档的渲染时端口（非 JAX-RS）。
 *
 * <p>对外 HTTP 由 {@link DefaultPageDslEndpoint} 以 {@code @Path} 暴露。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface PageDslEndpoint {

    /**
     * 加载并准备一个页面 DSL 文档。
     *
     * @param domainKey 项目领域键（classpath {@code ui-pages} 首段）
     * @param moduleKey 所属模块键
     * @param pageKey 与 {@code page.id} 匹配的页面键
     * @param parameters 运行时请求参数，供过滤器写入 {@code state}
     * @return 经过滤器链处理后的页面 DSL 文档
     */
    PageDsl render(String domainKey, String moduleKey, String pageKey, Map<String, Object> parameters);
}
