package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.DslNodeDeserializer;

import lombok.Getter;
import lombok.Setter;

/**
 * 从服务或 HTTP 源加载的动态 DSL 片段。
 *
 * <p>加载 UI 结构而非业务数据。远程 DSL 加载期间可使用 {@link #placeholder} 渲染回退节点。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class DslSourceRef implements DslRenderable {

    private DslSource source;

    @JsonDeserialize(using = DslNodeDeserializer.class)
    private DslNode placeholder;
}
