package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.PageDsl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Render-time context passed through a {@link PageDslFilterChain}.
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
     * Creates a render context for one module page.
     *
     * @param moduleKey owning module key
     * @param pageKey page key matching {@code page.id}
     * @param document source page DSL loaded from storage
     * @param parameters runtime request parameters
     * @return render context
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

    /** Returns the owning module key. */
    public String moduleKey() {
        return moduleKey;
    }

    /** Returns the page key. */
    public String pageKey() {
        return pageKey;
    }

    /** Returns the current working page DSL document. */
    public PageDsl document() {
        return document;
    }

    /**
     * Returns a context view with a replaced working document.
     *
     * @param document next working document
     * @return context for the next filter step
     */
    public PageDslRenderContext withDocument(PageDsl document) {
        if (document == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl render context requires a document");
        }
        return new PageDslRenderContext(moduleKey, pageKey, document, parameters, attributes);
    }

    /** Returns an immutable view of request parameters. */
    public Map<String, Object> parameters() {
        return parameters;
    }

    /**
     * Stores one attribute for downstream filters in the same chain execution.
     *
     * @param key attribute key
     * @param value attribute value
     * @return this context for fluent chaining
     */
    public PageDslRenderContext attribute(String key, Object value) {
        if (key != null) {
            attributes.put(key, value);
        }
        return this;
    }

    /** Returns the attribute value, or {@code null} when absent. */
    public Object attribute(String key) {
        return attributes.get(key);
    }

    /** Returns an immutable view of chain attributes. */
    public Map<String, Object> attributes() {
        return Map.copyOf(attributes);
    }
}
