package com.innospots.nexus.platform.access.domain.entity;

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
import com.innospots.nexus.platform.access.domain.enums.PlatformAccessRequestStatus;

/**
 * 平台注册待审核申请（{@code nx_pl_access_request}），与 {@code PENDING_APPROVAL} 用户关联。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus#PENDING_APPROVAL
 */
@Getter
@Setter
@Entity
@Table(name = PlatformAccessRequestEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_pl_access_request_status", columnList = "status")
})
@TableName(PlatformAccessRequestEntity.TABLE_NAME)
public class PlatformAccessRequestEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_pl_access_request";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String accessRequestId;

    @Override
    public String idPrefix() {
        return "par";
    }

    @Column(length = 128, nullable = false)
    private String applicantName;

    @Column(length = 64)
    private String loginName;

    @Column(length = 32)
    private String platformUserId;

    @Column(length = 128)
    private String email;

    @Column(length = 32)
    private String mobile;

    @Column(length = 512)
    private String description;

    @Column(length = 32, nullable = false)
    private String status;

    @Column(length = 512)
    private String rejectReason;

    /** 审批通过时不再创建邀请；保留列供历史/迁移，当前流程不写入。 */
    @Column(length = 32)
    private String approvedInviteId;

    @Column
    private LocalDateTime reviewedAt;

    @Column(length = 64)
    private String reviewedBy;

    public PlatformAccessRequestStatus statusEnum() {
        return PlatformAccessRequestStatus.valueOf(status);
    }
}
