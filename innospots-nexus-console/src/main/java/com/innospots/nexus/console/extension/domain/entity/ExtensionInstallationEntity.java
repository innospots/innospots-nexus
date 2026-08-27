package com.innospots.nexus.console.extension.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.innospots.nexus.core.persistence.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Persistent installation and registration record for one extension key.
 * The row remains after a JAR is removed so the management platform can retain
 * enablement intent, diagnostics, and permission-resource identity.
 */
@Getter
@Setter
@Entity
@Table(name = ExtensionInstallationEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nexus_ext_install_key", columnList = "extension_key", unique = true),
        @Index(name = "idx_nexus_ext_install_state", columnList = "state")
})
@TableName(ExtensionInstallationEntity.TABLE_NAME)
public class ExtensionInstallationEntity extends BaseEntity {

    /** Database table name. */
    public static final String TABLE_NAME = "nexus_extension_installation";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(name = "installation_id", length = 32, nullable = false)
    private String installationId;

    @Column(name = "extension_key", length = 256, nullable = false, updatable = false)
    private String extensionKey;

    @Column(name = "extension_version", length = 64, nullable = false)
    private String extensionVersion;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "state", length = 32, nullable = false)
    private String state;

    @Lob
    @Column(name = "descriptor_snapshot")
    private String descriptorSnapshot;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    @Column(name = "discovered_time")
    private LocalDateTime discoveredTime;

    @Column(name = "activated_time")
    private LocalDateTime activatedTime;

    @Column(name = "disabled_time")
    private LocalDateTime disabledTime;

    @Override
    public String idPrefix() {
        return "ext";
    }
}
