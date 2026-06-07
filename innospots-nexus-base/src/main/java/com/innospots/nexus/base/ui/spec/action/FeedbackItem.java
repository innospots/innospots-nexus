package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FeedbackItem {

    private String status;
    private I18nObject message = new I18nObject();

    public FeedbackItem() {
    }

    public static FeedbackItem of(String status, I18nObject message) {
        FeedbackItem item = new FeedbackItem();
        item.status = status;
        item.message = message == null ? new I18nObject() : message;
        return item;
    }

    public String status() {
        return status;
    }

    public I18nObject message() {
        return message;
    }
}
