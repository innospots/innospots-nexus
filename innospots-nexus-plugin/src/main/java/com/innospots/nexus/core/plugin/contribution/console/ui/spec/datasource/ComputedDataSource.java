package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PaginationConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Protocol-reserved computed data source. Runtime semantics are implementation-defined.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ComputedDataSource implements DataSourceConfig {

    private String type = "computed";
    private String expression;
    private List<String> dependsOn = new ArrayList<>();
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    @JsonAnyGetter
    public Map<String, Object> extensions() {
        return extensions;
    }

    @JsonAnySetter
    public void extension(String key, Object value) {
        extensions.put(key, value);
    }

    @Override
    public Boolean getAutoLoad() {
        return null;
    }

    @Override
    public PaginationConfig getPagination() {
        return null;
    }

    @Override
    public String getValueField() {
        return null;
    }

    @Override
    public String getLabelField() {
        return null;
    }

    @Override
    public String getDisabledField() {
        return null;
    }
}
