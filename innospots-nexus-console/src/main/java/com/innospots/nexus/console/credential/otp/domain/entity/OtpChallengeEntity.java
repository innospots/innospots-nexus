package com.innospots.nexus.console.credential.otp.domain.entity;

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
 * 短生命周期 OTP 挑战行（表 {@value #TABLE_NAME}）；库内仅存 {@code codeVerifier} 哈希，明文码仅经 {@link com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent} 投递。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService
 * @see com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose
 * @see OwnershipEntity
 */
@Getter
@Setter
@Entity
@Table(name = OtpChallengeEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_otp_challenge_lookup",
                columnList = "owner_type,owner_id,security_realm,purpose,channel,destination"),
        @Index(name = "idx_nx_otp_challenge_expires", columnList = "expires_at")
})
@TableName(OtpChallengeEntity.TABLE_NAME)
public class OtpChallengeEntity extends OwnershipEntity {

    /**
     * 物理表名 {@code nx_otp_challenge}。
     */
    public static final String TABLE_NAME = "nx_otp_challenge";

    /**
     * 挑战主键；前缀 {@code och}（见 {@link #idPrefix()}）。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String challengeId;

    /**
     * Console 侧 ID 生成前缀，与 {@link #challengeId} 分配策略一致。
     *
     * @return 固定值 {@code och}
     */
    @Override
    public String idPrefix() {
        return "och";
    }

    /**
     * 业务用途，存 {@link com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose} 的 {@code name()}。
     */
    @Column(length = 32, nullable = false)
    private String purpose;

    /**
     * 下发通道，存 {@link com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel} 的 {@code name()}。
     */
    @Column(length = 32, nullable = false)
    private String channel;

    /**
     * 规范化后的投递地址（邮箱小写、手机号去空格），作为查询与冷却键。
     */
    @Column(length = 256, nullable = false)
    private String destination;

    /**
     * OTP 码哈希算法 ID（当前固定为 BCrypt 材料，标识 {@code otp-bcrypt@v1}）。
     */
    @Column(length = 64, nullable = false)
    private String algorithm;

    /**
     * 验证码单向哈希；禁止存明文。
     */
    @Column(length = 512, nullable = false)
    private String codeVerifier;

    /**
     * 挑战过期时间；过期后校验返回 {@link com.innospots.nexus.console.credential.otp.status.OtpStatusCode#CHALLENGE_EXPIRED}。
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 消费时间；非 null 表示挑战已作废或已成功收尾。
     */
    @Column
    private LocalDateTime consumedAt;

    /**
     * 当前错误校验次数；达 {@link #maxAttempts} 后挑战作废。
     */
    @Column(nullable = false)
    private Integer attemptCount;

    /**
     * 允许的最大错误校验次数（来自 {@link com.innospots.nexus.console.credential.otp.policy.OtpPolicy}）。
     */
    @Column(nullable = false)
    private Integer maxAttempts;

    /**
     * 同一目的地累计重发次数（含本次发放前的作废挑战）。
     */
    @Column(nullable = false)
    private Integer resendCount;
}
