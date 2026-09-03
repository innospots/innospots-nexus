package com.innospots.nexus.base.ui.spec.endpoint;

import com.innospots.nexus.base.ui.spec.UiSpec;

import java.util.Map;

/**
 * Contract for resolving a render-ready page specification.
 * Implementations load the source YAML specification, run the configured
 * {@link com.innospots.nexus.base.ui.spec.filter.UiSpecFilterChain},
 * and return the final structure consumed by frontend renderers.
 *
 * <p>HTTP binding belongs in adapter modules; this interface stays
 * framework-neutral.</p>
 *
 * @see DefaultUiSpecEndpoint
 */
public interface UiSpecEndpoint {

    /**
     * Loads and prepares one page specification for frontend rendering.
     *
     * @param moduleKey owning module key
     * @param pageKey page key matching {@code pageInfo.pageId}
     * @param parameters runtime request parameters used by filters
     * @return processed page specification
     */
    UiSpec render(String moduleKey, String pageKey, Map<String, Object> parameters);
}
