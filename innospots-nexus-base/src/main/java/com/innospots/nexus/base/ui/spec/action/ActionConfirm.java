package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActionConfirm {

    private I18nObject title = new I18nObject();
    private I18nObject message = new I18nObject();

    public ActionConfirm() {
    }

    public static ActionConfirm of(I18nObject title, I18nObject message) {
        ActionConfirm confirm = new ActionConfirm();
        confirm.title = title == null ? new I18nObject() : title;
        confirm.message = message == null ? new I18nObject() : message;
        return confirm;
    }

    public I18nObject title() {
        return title;
    }

    public I18nObject message() {
        return message;
    }
}
