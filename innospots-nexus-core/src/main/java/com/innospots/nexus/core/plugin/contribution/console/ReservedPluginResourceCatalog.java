package com.innospots.nexus.core.plugin.contribution.console;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** 由 Core 安全快照提供的历史插件资源身份保留目录。 */
public final class ReservedPluginResourceCatalog {

    private final Set<ReservedResource> resources;

    /** 创建不可变的历史资源保留目录。 */
    public ReservedPluginResourceCatalog(Collection<ReservedResource> resources) {
        this.resources = resources == null ? Set.of() : Set.copyOf(resources);
    }

    /** 返回某插件是否仍拥有指定的历史资源身份。 */
    public boolean isReserved(String ownerPluginId, String resourceType, String resourceKey) {
        return resources.contains(new ReservedResource(ownerPluginId, resourceType, resourceKey));
    }

    /** 返回指定资源身份的历史归属插件。 */
    public Optional<String> ownerOf(String resourceType, String resourceKey) {
        return resources.stream()
                .filter(value -> value.resourceType().equals(resourceType)
                        && value.resourceKey().equals(resourceKey))
                .map(ReservedResource::ownerPluginId)
                .findFirst();
    }

    /** 返回不可变保留资源快照。 */
    public List<ReservedResource> resources() {
        return resources.stream().toList();
    }

    /** 一项不含实现类和运行时对象的历史资源身份。 */
    public record ReservedResource(String ownerPluginId, String resourceType, String resourceKey) {
    }
}
