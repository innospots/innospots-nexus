package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActionFeedback {

    private List<FeedbackItem> items = new ArrayList<>();

    public ActionFeedback() {
    }

    public static ActionFeedback create() {
        return new ActionFeedback();
    }

    public List<FeedbackItem> items() {
        return List.copyOf(items);
    }

    public ActionFeedback item(FeedbackItem item) {
        if (item != null) {
            items.add(item);
        }
        return this;
    }
}
