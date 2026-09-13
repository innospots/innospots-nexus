package com.innospots.nexus.core.plugin.installation.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.innospots.nexus.core.persistence.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Core 持有的全局插件安装事实表映射，不包含租户或工作区字段。
 *
 * <p>{@code definition_snapshot} 与 {@code last_error} 仅存储脱敏后的 JSON 或诊断文本。</p>
 */
@Getter
@Setter
@Entity
@Table(name = PluginInstallationEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_plugin_installation_plugin_id", columnList = "plugin_id", unique = true),
        @Index(name = "idx_nx_plugin_installation_presence", columnList = "presence"),
        @Index(name = "idx_nx_plugin_installation_enablement", columnList = "installed,desired_enabled")
})
@TableName(PluginInstallationEntity.TABLE_NAME)
public class PluginInstallationEntity extends BaseEntity {

    /** 数据库表名。 */
    public static final String TABLE_NAME = "nx_plugin_installation";

    /** 安装记录主键。 */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(name = "installation_id", length = 32, nullable = false)
    private String installationId;

    /** 稳定的插件标识；与 classpath 定义一一对应。 */
    @Column(name = "plugin_id", length = 256, nullable = false, updatable = false)
    private String pluginId;

    @Column(name = "plugin_version", length = 64, nullable = false)
    private String pluginVersion;

    @Column(name = "source_type", length = 16, nullable = false)
    private String sourceType;

    @Column(name = "source_location", length = 1024)
    private String sourceLocation;

    /** {@link com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence} 持久化值。 */
    @Column(name = "presence", length = 16, nullable = false)
    private String presence;

    /** 是否已执行安装动作（与 desiredEnabled 独立）。 */
    @Column(name = "installed", nullable = false)
    private boolean installed;

    /** 管理员期望启用；实际运行态由 JVM 内 {@code PluginState} 决定。 */
    @Column(name = "desired_enabled", nullable = false)
    private boolean desiredEnabled;

    /** 脱敏后的定义 JSON；不含 Secret 与运行时对象。 */
    @Lob
    @Column(name = "definition_snapshot")
    private String definitionSnapshot;

    /** 最近一次运行诊断状态名；不参与下次启动决策。 */
    @Column(name = "last_runtime_state", length = 32)
    private String lastRuntimeState;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    @Column(name = "first_discovered_at", nullable = false)
    private LocalDateTime firstDiscoveredAt;

    @Column(name = "last_discovered_at", nullable = false)
    private LocalDateTime lastDiscoveredAt;

    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    @Column(name = "missing_at")
    private LocalDateTime missingAt;

    @Override
    public String idPrefix() {
        return "plg";
    }
}
