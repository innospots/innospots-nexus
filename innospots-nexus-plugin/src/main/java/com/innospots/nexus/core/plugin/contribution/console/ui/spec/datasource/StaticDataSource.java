package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * 文档内静态数据源，用于枚举、固定配置与演示数据。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class StaticDataSource extends OptionMappingDataSource implements DataSourceConfig {

    private String type = "static";
    private Object value;
}
