package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * 返回分页结果的数据源的分页绑定配置。
 *
 * <p>字段值可为字面量，或绑定到页面状态的表达式。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PaginationConfig {

    /** 当前页码或表达式。 */
    private Object page;

    /** 每页大小或表达式。 */
    private Object pageSize;

    /** 后端响应中包含总数的字段名。 */
    private String totalField;

    /** 后端响应中包含分页记录的字段名。 */
    private String dataField;
}
