package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.HttpRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link DslSourceRef} 的动态 DSL 源定义。
 *
 * @author Smars
 * @date 2026/09/13
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DslServiceSource.class, name = "service"),
        @JsonSubTypes.Type(value = DslHttpSource.class, name = "http")
})
public sealed interface DslSource permits DslServiceSource, DslHttpSource {

    /**
     * 返回源类型鉴别器。
     *
     * @return 源类型
     */
    String getType();
}
