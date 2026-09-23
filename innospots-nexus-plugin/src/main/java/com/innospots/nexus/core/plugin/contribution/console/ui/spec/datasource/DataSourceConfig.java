package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PaginationConfig;

/**
 * 命名页面数据源配置。
 *
 * <p>{@code type} 鉴别器选择允许的实现之一。选项字段映射
 * （{@link #getValueField()}、{@link #getLabelField()}、{@link #getDisabledField()}）
 * 在运行时将后端记录规范化为下拉选项。</p>
 *
 * @see StaticDataSource
 * @see ServiceDataSource
 * @see HttpDataSource
 * @author Smars
 * @date 2026/09/13
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
     * 返回数据源类型鉴别器。
     *
     * @return 数据源类型
     */
    String getType();

    /**
     * 返回运行时是否应自动加载此数据源。
     *
     * @return 自动加载标志
     */
    Boolean getAutoLoad();

    /**
     * 返回可选的分页绑定配置。
     *
     * @return 分页配置
     */
    PaginationConfig getPagination();

    /**
     * 返回用于选项规范化的后端值字段名。
     *
     * @return 值字段名
     */
    String getValueField();

    /**
     * 返回用于选项规范化的后端标签字段名。
     *
     * @return 标签字段名
     */
    String getLabelField();

    /**
     * 返回用于选项规范化的后端禁用字段名。
     *
     * @return 禁用字段名
     */
    String getDisabledField();
}
