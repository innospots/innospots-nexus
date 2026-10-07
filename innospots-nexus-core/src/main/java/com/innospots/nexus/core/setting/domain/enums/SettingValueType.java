package com.innospots.nexus.core.setting.domain.enums;

/**
 * 设置值逻辑类型（持久化为 {@code nx_system_setting.value_type} 字符串码）。
 *
 * <p>取值均存于 {@code setting_value} 文本列；类型仅约束格式校验与 UI/管理端展示语义。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity#valueType
 */
public enum SettingValueType {

    /** 普通字符串。 */
    STRING,

    /** 数值（十进制，存为文本）。 */
    NUMBER,

    /** {@code true} / {@code false}（不区分大小写）。 */
    BOOLEAN,

    /** 加密或脱敏后的密文字符串（opaque，不做明文语义校验）。 */
    ENCRYPTED,

    /** JSON 对象或数组文本。 */
    JSON
}
