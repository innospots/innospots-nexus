package com.innospots.nexus.base.ui.spec.endpoint;

import com.innospots.nexus.base.ui.spec.PageDsl;

import java.util.Map;

/** Render-time API for loading and preparing page DSL documents. */
public interface PageDslEndpoint {

    /**
     * Loads and prepares one page DSL document.
     *
     * @param moduleKey owning module key
     * @param pageKey page key matching {@code page.id}
     * @param parameters runtime request parameters
     * @return prepared page DSL document
     */
    PageDsl render(String moduleKey, String pageKey, Map<String, Object> parameters);
}
