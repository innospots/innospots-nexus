package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 在 {@link PageDslFilterChain} 中传递的渲染时上下文。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class PageDslRenderContext {

    private final String moduleKey;
    private final String pageKey;
    private final PageDsl document;
    private final Map<String, Object> parameters;
    private final Map<String, Object> attributes;

    private PageDslRenderContext(
            String moduleKey,
            String pageKey,
            PageDsl document,
            Map<String, Object> parameters,
            Map<String, Object> attributes
    ) {
        this.moduleKey = moduleKey;
        this.pageKey = pageKey;
        this.document = document;
        this.parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        this.attributes = attributes == null ? new LinkedHashMap<>() : attributes;
    }

    /**
     * 为一个模块页面创建渲染上下文。
     *
     * @param moduleKey 所属模块键
     * @param pageKey 与 {@code page.id} 匹配的页面键
     * @param document 从存储加载的源页面 DSL
     * @param parameters 运行时请求参数
     * @return 渲染上下文
     * @throws com.innospots.nexus.base.exception.NexusException 文档为 null 时
     */
    public static PageDslRenderContext of(
            String moduleKey,
            String pageKey,
            PageDsl document,
            Map<String, Object> parameters
    ) {
        if (document == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl render context requires a document");
        }
        return new PageDslRenderContext(moduleKey, pageKey, document, parameters, new LinkedHashMap<>());
    }

    /**
     * 返回所属模块键。
     *
     * @return 模块键
     */
    public String moduleKey() {
        return moduleKey;
    }

    /**
     * 返回页面键。
     *
     * @return 页面键
     */
    public String pageKey() {
        return pageKey;
    }

    /**
     * 返回当前工作页面 DSL 文档。
     *
     * @return 页面 DSL 文档
     */
    public PageDsl document() {
        return document;
    }

    /**
     * 返回替换了工作文档的上下文视图。
     *
     * @param document 下一工作文档
     * @return 供下一过滤器步骤使用的上下文
     * @throws com.innospots.nexus.base.exception.NexusException 文档为 null 时
     */
    public PageDslRenderContext withDocument(PageDsl document) {
        if (document == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl render context requires a document");
        }
        return new PageDslRenderContext(moduleKey, pageKey, document, parameters, attributes);
    }

    /**
     * 返回请求参数的不可变视图。
     *
     * @return 请求参数
     */
    public Map<String, Object> parameters() {
        return parameters;
    }

    /**
     * 为同一链执行中的下游过滤器存储一个属性。
     *
     * @param key 属性键
     * @param value 属性值
     * @return 本上下文，支持链式调用
     */
    public PageDslRenderContext attribute(String key, Object value) {
        if (key != null) {
            attributes.put(key, value);
        }
        return this;
    }

    /**
     * 返回属性值；不存在时返回 {@code null}。
     *
     * @param key 属性键
     * @return 属性值
     */
    public Object attribute(String key) {
        return attributes.get(key);
    }

    /**
     * 返回链属性的不可变视图。
     *
     * @return 链属性
     */
    public Map<String, Object> attributes() {
        return Map.copyOf(attributes);
    }
}
