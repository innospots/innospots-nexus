package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PaginationConfig;

import lombok.Getter;
import lombok.Setter;

/** 具体数据源类型共享的选项映射与自动加载字段。 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
abstract class OptionMappingDataSource {

    private Boolean autoLoad;
    private PaginationConfig pagination;
    private String valueField;
    private String labelField;
    private String disabledField;
}
