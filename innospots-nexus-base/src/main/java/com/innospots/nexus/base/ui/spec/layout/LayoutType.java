package com.innospots.nexus.base.ui.spec.layout;

public enum LayoutType {
    GRID("grid"),
    ASIDE("asider"),
    THREE_COLUMN("three-column"),
    TAB("tab");

    private final String code;

    LayoutType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
