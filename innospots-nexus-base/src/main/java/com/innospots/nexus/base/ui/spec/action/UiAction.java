package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.ui.spec.ApiRequest;
import com.innospots.nexus.base.ui.spec.VisibleCondition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiAction {

    private String actionId;
    private String actionType;
    private I18nObject label = new I18nObject();
    private String icon;
    private String scope;
    private String target;
    private I18nObject tooltip = new I18nObject();
    private VisibleCondition visibleIf;
    private ActionPermissions permissions;
    private ActionStyle style;
    private ApiRequest request;
    private ActionConfirm confirm;
    private ActionFeedback feedback;
    private List<UiAction> children = new ArrayList<>();
    private Map<String, Object> properties = new LinkedHashMap<>();

    public UiAction() {
    }

    public static UiAction of(String actionId, ActionType actionType) {
        UiAction action = new UiAction();
        action.actionId = actionId;
        action.actionType = actionType == null ? null : actionType.code();
        return action;
    }

    public String actionId() {
        return actionId;
    }

    public String actionType() {
        return actionType;
    }

    public I18nObject label() {
        return label;
    }

    public UiAction label(I18nObject label) {
        this.label = label == null ? new I18nObject() : label;
        return this;
    }

    public String icon() {
        return icon;
    }

    public UiAction icon(String icon) {
        this.icon = icon;
        return this;
    }

    public String scope() {
        return scope;
    }

    public UiAction scope(String scope) {
        this.scope = scope;
        return this;
    }

    public String target() {
        return target;
    }

    public UiAction target(String target) {
        this.target = target;
        return this;
    }

    public I18nObject tooltip() {
        return tooltip;
    }

    public UiAction tooltip(I18nObject tooltip) {
        this.tooltip = tooltip == null ? new I18nObject() : tooltip;
        return this;
    }

    public VisibleCondition visibleIf() {
        return visibleIf;
    }

    public UiAction visibleIf(VisibleCondition visibleIf) {
        this.visibleIf = visibleIf;
        return this;
    }

    public ActionPermissions permissions() {
        return permissions;
    }

    public UiAction permissions(ActionPermissions permissions) {
        this.permissions = permissions;
        return this;
    }

    public ActionStyle style() {
        return style;
    }

    public UiAction style(ActionStyle style) {
        this.style = style;
        return this;
    }

    public ApiRequest request() {
        return request;
    }

    public UiAction request(ApiRequest request) {
        this.request = request;
        return this;
    }

    public ActionConfirm confirm() {
        return confirm;
    }

    public UiAction confirm(ActionConfirm confirm) {
        this.confirm = confirm;
        return this;
    }

    public ActionFeedback feedback() {
        return feedback;
    }

    public UiAction feedback(ActionFeedback feedback) {
        this.feedback = feedback;
        return this;
    }

    public List<UiAction> children() {
        return List.copyOf(children);
    }

    public UiAction child(UiAction action) {
        if (action != null) {
            children.add(action);
        }
        return this;
    }

    public Map<String, Object> properties() {
        return Map.copyOf(properties);
    }

    public UiAction property(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
        return this;
    }
}
