package com.innospots.nexus.console.sitemap.domain.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * 布局 DSL 内联组件节点；{@link #props} 与 {@link #events} 保持开放结构。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapLayoutNode", description = "布局 DSL 组件节点")
public final class SitemapLayoutNode {

    @Schema(description = "组件类型", required = true)
    private String type;

    @Schema(description = "可选节点 id")
    private String id;

    @Schema(description = "组件属性（开放 map）")
    private Map<String, Object> props = new LinkedHashMap<>();

    @Schema(description = "子节点")
    private List<SitemapLayoutNode> children = new ArrayList<>();

    @Schema(description = "事件处理器（开放 map）")
    private Map<String, Object> events = new LinkedHashMap<>();
}
