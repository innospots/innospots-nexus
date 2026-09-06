package com.innospots.nexus.base.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.PaginationConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Protocol-reserved resource data source. Runtime semantics are implementation-defined.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ResourceDataSource implements DataSourceConfig {

    private String type = "resource";
    private String resource;
    private Map<String, Object> params = new LinkedHashMap<>();
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
