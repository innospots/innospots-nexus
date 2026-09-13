package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Service-registry-backed data source. Preferred for business pages because DSL does not
 * bind directly to HTTP endpoints.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ServiceDataSource extends OptionMappingDataSource implements DataSourceConfig {

    private String type = "service";
    private String service;
    private Map<String, Object> params = new LinkedHashMap<>();
}
