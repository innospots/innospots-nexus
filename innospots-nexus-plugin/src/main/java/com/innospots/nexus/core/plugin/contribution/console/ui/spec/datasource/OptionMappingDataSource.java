package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PaginationConfig;

import lombok.Getter;
import lombok.Setter;

/** Shared option-mapping and auto-load fields for concrete data source types. */
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
