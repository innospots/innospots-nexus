package com.innospots.nexus.console.credential.password.domain.entity;

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
 * 按控制台归属存储的用户鉴证凭据行（表 {@value #TABLE_NAME}）。
 * <p>同一主体在同一安全域下每种 {@link com.innospots.nexus.console.credential.password.CredentialKind}
 * 至多一行（见唯一索引 {@code uk_nx_user_credential_subject}）。
 * {@code verifier} / {@code verifierParams} 为 opaque，由 {@code algorithm} 与种类解释，不得存明文密码、OTP 或 TOTP 共享密钥。</p>
 * <p>读写编排见 {@link com.innospots.nexus.console.credential.password.operator.UserCredentialOperator}；
 * 登录快照见 {@link com.innospots.nexus.console.auth.domain.model.CredentialRecord}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.auth.domain.model.CredentialRecord
 * @see com.innospots.nexus.console.credential.password.CredentialKind
 * @see OwnershipEntity
 */
@Getter
@Setter
@Entity
@Table(name = UserCredentialEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_user_credential_subject",
                columnList = "owner_type,owner_id,security_realm,credential_kind,subject_id", unique = true),
        @Index(name = "idx_nx_user_credential_subject", columnList = "subject_id,credential_kind")
})
@TableName(UserCredentialEntity.TABLE_NAME)
public class UserCredentialEntity extends OwnershipEntity {

    /**
     * 物理表名 {@code nx_user_credential}。
     */
    public static final String TABLE_NAME = "nx_user_credential";

    /**
     * 凭据行主键；前缀 {@code ucr}（见 {@link #idPrefix()}）。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String credentialId;

    @Override
    public String idPrefix() {
        return "ucr";
    }

    /**
     * 凭据所属用户主体 ID（平台用户或租户用户，与登录主体一致）。
     */
    @Column(length = 32, nullable = false)
    private String subjectId;

    /**
     * 凭据种类，存 {@link com.innospots.nexus.console.credential.password.CredentialKind} 的 {@code name()}（如 {@code PASSWORD}、{@code TOTP}）。
     */
    @Column(length = 32, nullable = false)
    private String credentialKind;

    /**
     * 验证算法稳定 ID（如密码 {@link com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithms#BCRYPT_V1}、
     * TOTP {@link com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithms#RFC6238_SHA1_V1}）。
     */
    @Column(length = 64, nullable = false)
    private String algorithm;

    /**
     * 不透明验证材料：密码哈希、TOTP 加密后的共享密钥等；禁止明文。
     */
    @Column(length = 512, nullable = false)
    private String verifier;

    /**
     * 算法或种类相关的扩展参数（console 层不解析语义）。
     * <p>例如 TOTP 注册待确认时存 {@link com.innospots.nexus.console.credential.totp.TotpCredentials#ENROLLMENT_PENDING}，
     * 激活后可为 null。</p>
     */
    @Column(length = 1024)
    private String verifierParams;

    /**
     * 凭据材料版本；改密或轮换 verifier 时递增，用于审计与并发判断。
     */
    @Column(nullable = false)
    private Integer credentialVersion;

    /**
     * 为 {@code true} 时要求用户下次成功认证后强制改密（仅 {@code PASSWORD} 种类常用）。
     */
    @Column(nullable = false)
    private Boolean forceReset;

    /**
     * 连续验证失败次数；成功登录或管理员解锁后归零。
     */
    @Column(nullable = false)
    private Integer failedAttempts;

    /**
     * 账户锁定截止时间；{@code null} 表示未锁定。与 {@link #failedAttempts} 配合实现登录防暴力。
     */
    @Column
    private LocalDateTime lockedUntil;

    /**
     * 凭据过期时间；{@code null} 表示无过期策略（密码/TOTP 通常为空，预留临时凭据场景）。
     */
    @Column
    private LocalDateTime expiredAt;
}
