package com.innospots.nexus.base.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.HttpRequest;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP-backed data source. Use when a page must call a concrete HTTP endpoint directly.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class HttpDataSource extends OptionMappingDataSource implements DataSourceConfig {

    private String type = "http";
    private HttpRequest request;
}
