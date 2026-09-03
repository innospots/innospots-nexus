package com.innospots.nexus.base.ui.spec.endpoint;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.filter.UiSpecFilterChain;
import com.innospots.nexus.base.ui.spec.filter.UiSpecRenderContext;
import com.innospots.nexus.base.ui.spec.loader.UiSpecLoader;

import java.util.Map;

/**
 * Default {@link UiSpecEndpoint} that loads a classpath specification and
 * runs it through a configured filter chain.
 */
public class DefaultUiSpecEndpoint implements UiSpecEndpoint {

    private final UiSpecLoader loader;
    private final UiSpecFilterChain filterChain;

    /**
     * Creates an endpoint with the supplied loader and filter chain.
     *
     * @param loader specification loader
     * @param filterChain ordered render-time filters
     */
    public DefaultUiSpecEndpoint(UiSpecLoader loader, UiSpecFilterChain filterChain) {
        if (loader == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec loader is required");
        }
        this.loader = loader;
        this.filterChain = filterChain == null ? UiSpecFilterChain.create() : filterChain;
    }

    @Override
    public UiSpec render(String moduleKey, String pageKey, Map<String, Object> parameters) {
        if (!hasText(moduleKey) || !hasText(pageKey)) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec moduleKey and pageKey are required");
        }
        UiSpec spec = loader.load(moduleKey, pageKey);
        UiSpecRenderContext context = UiSpecRenderContext.of(moduleKey, pageKey, spec, parameters);
        return filterChain.process(context);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
