package com.innospots.nexus.base.domain.field;

/**
 * 字段在领域模式中的角色或归属边界。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DomainField
 */
public enum FieldScope {
    /** 输入字段 */
    INPUT,
    /** 输出字段 */
    OUTPUT,
    /** 参数字段 */
    PARAMETER,
    /** 元数据字段 */
    METADATA
}
