package com.innospots.nexus.base.ui.spec.action;

public enum ActionType {
    REFRESH("refresh"),
    API("api"),
    DOWNLOAD("download"),
    LINK("link"),
    COMPONENT("component"),
    MODAL("modal"),
    FORM("form"),
    DROPDOWN("dropdown"),
    TOGGLE("toggle"),
    STATUS("status"),
    PAGE("page"),
    POP("pop"),
    CLOSE_MODAL("closeModal"),
    AI("ai"),
    IMPORT("import");

    private final String code;

    ActionType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
