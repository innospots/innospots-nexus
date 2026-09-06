package com.innospots.nexus.base.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.innospots.nexus.base.ui.spec.PaginationConfig;

/**
 * Named page data source configuration.
 *
 * <p>The {@code type} discriminator selects one of the permitted implementations. Option field
 * mappings ({@link #getValueField()}, {@link #getLabelField()}, {@link #getDisabledField()})
 * normalize backend records into select options at runtime.</p>
 *
 * @see StaticDataSource
 * @see ServiceDataSource
 * @see HttpDataSource
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StaticDataSource.class, name = "static"),
        @JsonSubTypes.Type(value = ServiceDataSource.class, name = "service"),
        @JsonSubTypes.Type(value = HttpDataSource.class, name = "http"),
        @JsonSubTypes.Type(value = ComputedDataSource.class, name = "computed"),
        @JsonSubTypes.Type(value = ResourceDataSource.class, name = "resource")
})
public sealed interface DataSourceConfig permits
        StaticDataSource,
        ServiceDataSource,
        HttpDataSource,
        ComputedDataSource,
        ResourceDataSource {

    /**
     * Returns the data source type discriminator.
     *
     * @return source type
     */
    String getType();

    /**
     * Returns whether the runtime should load this source automatically.
     *
     * @return auto-load flag
     */
    Boolean getAutoLoad();

    /**
     * Returns optional pagination bindings.
     *
     * @return pagination config
     */
    PaginationConfig getPagination();

    /**
     * Returns the backend value field for option normalization.
     *
     * @return value field
     */
    String getValueField();

    /**
     * Returns the backend label field for option normalization.
     *
     * @return label field
     */
    String getLabelField();

    /**
     * Returns the backend disabled field for option normalization.
     *
     * @return disabled field
     */
    String getDisabledField();
}
