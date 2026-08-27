package com.innospots.nexus.base.ui.spec.loader;

import com.innospots.nexus.base.ui.spec.UiSpec;

/** Loads one page specification by its module-local stable identity. */
public interface UiSpecLoader {

    /** Loads and parses one page specification. */
    UiSpec load(String moduleKey, String pageKey);
}
