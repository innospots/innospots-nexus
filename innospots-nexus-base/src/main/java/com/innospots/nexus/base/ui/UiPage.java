package com.innospots.nexus.base.ui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A page definition in the dynamic UI model. Groups {@link UiComponent}
 * instances under a page key and URL, with optional navigation visibility
 * and homepage flags.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiPage {

    private String pageKey;
    private String pageName;
    private String pageUrl;
    private Boolean showNavigation;
    private Boolean homePage;
    private Map<String, UiComponent> components = new LinkedHashMap<>();
    private Map<String, Object> configs = new LinkedHashMap<>();

    public UiPage() {
    }

    public static UiPage of(String pageKey, String pageName, String pageUrl) {
        UiPage page = new UiPage();
        page.pageKey = pageKey;
        page.pageName = pageName;
        page.pageUrl = pageUrl;
        return page;
    }

    public String pageKey() {
        return pageKey;
    }

    public String pageName() {
        return pageName;
    }

    public String pageUrl() {
        return pageUrl;
    }

    public Boolean showNavigation() {
        return showNavigation;
    }

    public UiPage showNavigation(Boolean showNavigation) {
        this.showNavigation = showNavigation;
        return this;
    }

    public Boolean homePage() {
        return homePage;
    }

    public UiPage homePage(Boolean homePage) {
        this.homePage = homePage;
        return this;
    }

    public Map<String, UiComponent> components() {
        return Map.copyOf(components);
    }

    public UiPage component(UiComponent component) {
        if (component != null) {
            components.put(component.name(), component);
        }
        return this;
    }

    public Map<String, Object> configs() {
        return Map.copyOf(configs);
    }

    public UiPage config(String key, Object value) {
        if (key != null) {
            configs.put(key, value);
        }
        return this;
    }
}
