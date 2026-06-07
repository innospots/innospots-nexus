package com.innospots.nexus.base.ui.spec.component;

public enum ComponentType {
    ALERT_CARD("alert-card"),
    BANNER_CARD("banner-card"),
    CHART("chart"),
    FORM("form"),
    GAUGE_CARD("gauge-card"),
    INFO_CARD("info-card"),
    LAYOUT("layout"),
    METRIC_CARD("metric-card"),
    PAGE("page"),
    PLAIN_TEXT("plain-text"),
    TABLE("table"),
    TEXT_CARD("text-card");

    private final String code;

    ComponentType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
