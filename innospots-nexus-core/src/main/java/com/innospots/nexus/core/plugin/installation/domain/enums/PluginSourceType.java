package com.innospots.nexus.core.plugin.installation.domain.enums;

/** 插件定义来源类型。 */
public enum PluginSourceType {

    /** Java SPI 或 classpath 实现类。 */
    JAVA,

    /** YAML 资源声明。 */
    YAML;

    /**
     * 将来源文本转换为枚举。
     *
     * @param value 持久化或声明中的来源文本
     * @return 匹配的枚举值；输入为 null 时返回 null
     * @throws IllegalArgumentException 文本不是已知来源类型时抛出
     */
    public static PluginSourceType from(String value) {
        return value == null ? null : valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }
}
