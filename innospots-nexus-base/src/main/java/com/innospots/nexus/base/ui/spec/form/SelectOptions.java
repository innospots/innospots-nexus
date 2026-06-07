package com.innospots.nexus.base.ui.spec.form;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.ApiRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SelectOptions {

    private List<OptionItem> items = new ArrayList<>();
    private ApiRequest datasource;
    private String valueField;
    private String labelField;

    public SelectOptions() {
    }

    public static SelectOptions create() {
        return new SelectOptions();
    }

    public List<OptionItem> items() {
        return List.copyOf(items);
    }

    public SelectOptions item(OptionItem item) {
        if (item != null) {
            items.add(item);
        }
        return this;
    }

    public ApiRequest datasource() {
        return datasource;
    }

    public SelectOptions datasource(ApiRequest datasource) {
        this.datasource = datasource;
        return this;
    }

    public String valueField() {
        return valueField;
    }

    public SelectOptions valueField(String valueField) {
        this.valueField = valueField;
        return this;
    }

    public String labelField() {
        return labelField;
    }

    public SelectOptions labelField(String labelField) {
        this.labelField = labelField;
        return this;
    }
}
