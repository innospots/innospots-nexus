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

import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * Append-only audit log record for management-console operations.
 * <p>Captures who did what, when, and with what outcome so console activity can
 * be traced and reviewed. Written through {@code AuditLogDao}; the recording
 * flow is wired by interceptor adapters outside this package.</p>
 */
@Getter
@Setter
@Entity
@Table(name = AuditLogEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_audit_log_time", columnList = "operated_time"),
        @Index(name = "idx_nx_audit_log_action", columnList = "action"),
        @Index(name = "idx_nx_audit_log_actor", columnList = "actor"),
        @Index(name = "idx_nx_audit_log_result", columnList = "execution_result")
})
@TableName(AuditLogEntity.TABLE_NAME)
public class AuditLogEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_audit_log";

    /**
     * Audit log record identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String auditLogId;

    @Override
    public String idPrefix() {
        return "alg";
    }

    /**
     * Business action that was performed.
     */
    @Column(length = 64, nullable = false)
    private String action;

    /**
     * Invocation or resource path, such as a class-method or route path.
     */
    @Column(length = 256, nullable = false)
    private String path;

    /**
     * Time the audited operation happened.
     */
    @Column(nullable = false)
    private LocalDateTime operatedTime;

    /**
     * Identity of the actor who performed the operation.
     */
    @Column(length = 64)
    private String actor;

    /**
     * Human-readable result or error message.
     */
    @Column(length = 512)
    private String message;

    /**
     * Status code of the outcome, e.g. a business or HTTP status code.
     */
    @Column(length = 32)
    private String statusCode;

    /**
     * Execution outcome, e.g. SUCCESS or FAILURE.
     */
    @Column(length = 32, nullable = false)
    private String executionResult;

    /**
     * Key parameters of the operation, serialized and masked when sensitive.
     */
    @Column(length = 2048)
    private String keyParameters;
}
