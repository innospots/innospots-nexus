package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PageInfo {

    private String pageId;
    private I18nObject title = new I18nObject();
    private I18nObject name = new I18nObject();
    private I18nObject description = new I18nObject();

    public PageInfo() {
    }

    public static PageInfo of(String pageId, I18nObject title) {
        PageInfo info = new PageInfo();
        info.pageId = pageId;
        info.title = title == null ? new I18nObject() : title;
        info.name = info.title;
        return info;
    }

    public String pageId() {
        return pageId;
    }

    public I18nObject title() {
        return title;
    }

    public I18nObject name() {
        return name;
    }

    public PageInfo name(I18nObject name) {
        this.name = name == null ? new I18nObject() : name;
        return this;
    }

    public I18nObject description() {
        return description;
    }

    public PageInfo description(I18nObject description) {
        this.description = description == null ? new I18nObject() : description;
        return this;
    }
}
