package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.UiSpec;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Render-time context passed through a {@link UiSpecFilterChain}.
 * Carries the working {@link UiSpec}, request parameters, and mutable
 * attributes that filters can share across the chain.
 */
public final class UiSpecRenderContext {

    private final String moduleKey;
    private final String pageKey;
    private final UiSpec spec;
    private final Map<String, Object> parameters;
    private final Map<String, Object> attributes;

    private UiSpecRenderContext(
            String moduleKey,
            String pageKey,
            UiSpec spec,
            Map<String, Object> parameters,
            Map<String, Object> attributes
    ) {
        this.moduleKey = moduleKey;
        this.pageKey = pageKey;
        this.spec = spec;
        this.parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        this.attributes = attributes == null ? new LinkedHashMap<>() : attributes;
    }

    /**
     * Creates a render context for one module page.
     *
     * @param moduleKey owning module key
     * @param pageKey page key matching {@code pageInfo.pageId}
     * @param spec source specification loaded from storage
     * @param parameters runtime request parameters
     * @return render context
     */
    public static UiSpecRenderContext of(
            String moduleKey,
            String pageKey,
            UiSpec spec,
            Map<String, Object> parameters
    ) {
        if (spec == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec render context requires a specification");
        }
        return new UiSpecRenderContext(moduleKey, pageKey, spec, parameters, new LinkedHashMap<>());
    }

    /** Returns the owning module key. */
    public String moduleKey() {
        return moduleKey;
    }

    /** Returns the page key. */
    public String pageKey() {
        return pageKey;
    }

    /** Returns the current working specification. */
    public UiSpec spec() {
        return spec;
    }

    /**
     * Returns a context view with a replaced working specification.
     * Request parameters and attributes are preserved.
     *
     * @param spec next working specification
     * @return context for the next filter step
     */
    public UiSpecRenderContext withSpec(UiSpec spec) {
        if (spec == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec render context requires a specification");
        }
        return new UiSpecRenderContext(moduleKey, pageKey, spec, parameters, attributes);
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
    public UiSpecRenderContext attribute(String key, Object value) {
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
