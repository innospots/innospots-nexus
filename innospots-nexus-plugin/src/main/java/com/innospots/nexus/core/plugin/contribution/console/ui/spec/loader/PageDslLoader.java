package com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

/** Loads page DSL documents from a backing store. */
public interface PageDslLoader {

    /**
     * Loads one page DSL document.
     *
     * @param moduleKey owning module key
     * @param pageKey page key matching {@code page.id}
     * @return page DSL document
     */
    PageDsl load(String moduleKey, String pageKey);
}
