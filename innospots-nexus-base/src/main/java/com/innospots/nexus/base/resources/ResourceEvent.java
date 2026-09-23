package com.innospots.nexus.base.resources;

import com.innospots.nexus.base.events.DomainEvent;

/**
 * 资源元数据保存/持久化时发布的领域事件。
 *
 * @author Smars
 * @date 2026/09/13
 * @param metaResource 已保存的资源元数据
 * @see MetaResource
 * @see DomainEvent
 */
public record ResourceEvent(MetaResource metaResource) implements DomainEvent {

    /**
     * 返回事件类型标识。
     *
     * @return 事件类型字符串
     */
    @Override
    public String eventType() {
        return "resource.meta.saved";
    }
}
