package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.HttpRequest;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP 后端数据源；页面需直接调用具体 HTTP 端点时使用。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class HttpDataSource extends OptionMappingDataSource implements DataSourceConfig {

    private String type = "http";
    private HttpRequest request;
}
