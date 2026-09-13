package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * Static in-document data source for enums, fixed configuration, and demo data.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class StaticDataSource extends OptionMappingDataSource implements DataSourceConfig {

    private String type = "static";
    private Object value;
}
