package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PaginationConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 协议预留的计算数据源；运行时语义由实现定义。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class ComputedDataSource implements DataSourceConfig {

    private String type = "computed";
    private String expression;
    private List<String> dependsOn = new ArrayList<>();
    private final Map<String, Object> extensions = new LinkedHashMap<>();

    /**
     * 返回扩展属性的 Jackson 序列化视图。
     *
     * @return 扩展属性映射
     */
    @JsonAnyGetter
    public Map<String, Object> extensions() {
        return extensions;
    }

    /**
     * 设置一个扩展属性。
     *
     * @param key 扩展属性键
     * @param value 扩展属性值
     */
    @JsonAnySetter
    public void extension(String key, Object value) {
        extensions.put(key, value);
    }

    @Override
    public Boolean getAutoLoad() {
        return null;
    }

    @Override
    public PaginationConfig getPagination() {
        return null;
    }

    @Override
    public String getValueField() {
        return null;
    }

    @Override
    public String getLabelField() {
        return null;
    }

    @Override
    public String getDisabledField() {
        return null;
    }
}
