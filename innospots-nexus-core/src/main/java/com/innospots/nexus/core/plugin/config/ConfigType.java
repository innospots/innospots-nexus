package com.innospots.nexus.core.plugin.config;

/** 插件配置支持的值类型。 */
public enum ConfigType {

    /** 任意文本值。 */
    STRING,

    /** 32 位有符号整数。 */
    INTEGER,

    /** 64 位有符号整数。 */
    LONG,

    /** 布尔值。 */
    BOOLEAN,

    /** 任意精度十进制数。 */
    DECIMAL,

    /** ISO-8601 持续时间字面量，例如 {@code PT30S}。 */
    DURATION,

    /** 绝对 URI。 */
    URI,

    /** 受限于声明枚举值的字符串。 */
    ENUM,

    /** 敏感值；禁止默认值，诊断输出必须遮罩。 */
    SECRET
}
