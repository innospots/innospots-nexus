package com.innospots.nexus.console.logger.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.OwnershipEntity;

/**
 * 管理控制台操作的仅追加审计日志；归属列记录操作发生的控制台上下文。
 */
@Getter
@Setter
@Entity
@Table(name = AuditLogEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_audit_log_time", columnList = "operated_time"),
        @Index(name = "idx_nx_audit_log_action", columnList = "action"),
        @Index(name = "idx_nx_audit_log_actor", columnList = "actor"),
        @Index(name = "idx_nx_audit_log_result", columnList = "execution_result"),
        @Index(name = "idx_nx_audit_log_owner_time", columnList = "owner_type,owner_id,operated_time"),
        @Index(name = "idx_nx_audit_log_realm_time", columnList = "security_realm,operated_time")
})
@TableName(AuditLogEntity.TABLE_NAME)
public class AuditLogEntity extends OwnershipEntity {

    public static final String TABLE_NAME = "nx_audit_log";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String auditLogId;

    @Override
    public String idPrefix() {
        return "alg";
    }

    @Column(length = 64, nullable = false)
    private String action;

    @Column(length = 256, nullable = false)
    private String path;

    @Column(nullable = false)
    private LocalDateTime operatedTime;

    @Column(length = 64)
    private String actor;

    @Column(length = 512)
    private String message;

    @Column(length = 32)
    private String statusCode;

    @Column(length = 32, nullable = false)
    private String executionResult;

    @Column(length = 2048)
    private String keyParameters;
}
