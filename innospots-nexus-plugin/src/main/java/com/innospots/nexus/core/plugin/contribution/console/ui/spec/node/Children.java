package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson.ChildrenDeserializer;

import java.util.List;

/**
 * 以数组或单个动态源引用声明的子可渲染节点。
 *
 * @param items 当 {@link #isArray()} 为 {@code true} 时的内联子节点
 * @param sourceRef YAML 值为单个 {@code source} 对象时的动态 DSL 源
 * @author Smars
 * @date 2026/09/13
 */
@JsonDeserialize(using = ChildrenDeserializer.class)
public record Children(List<DslRenderable> items, DslSourceRef sourceRef) {

    /**
     * 从内联可渲染列表创建子节点。
     *
     * @param items 子可渲染节点
     * @return 子节点包装器
     */
    public static Children ofItems(List<DslRenderable> items) {
        return new Children(items, null);
    }

    /**
     * 从单个动态源引用创建子节点。
     *
     * @param sourceRef 动态源引用
     * @return 子节点包装器
     */
    public static Children ofSourceRef(DslSourceRef sourceRef) {
        return new Children(List.of(), sourceRef);
    }

    /**
     * 返回子节点是否以数组形式声明。
     *
     * @return 数组子节点时返回 {@code true}
     */
    public boolean isArray() {
        return sourceRef == null;
    }

    /**
     * 返回内联子节点列表，可能为空。
     *
     * @return 子节点列表
     */
    public List<DslRenderable> items() {
        return items == null ? List.of() : List.copyOf(items);
    }
}
