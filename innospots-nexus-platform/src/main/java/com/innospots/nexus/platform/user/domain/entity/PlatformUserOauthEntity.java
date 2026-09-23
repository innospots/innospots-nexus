package com.innospots.nexus.platform.user.domain.entity;

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
 * 平台域用户的 OAuth 身份绑定。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@Entity
@Table(name = PlatformUserOauthEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_platform_user_oauth_user", columnList = "platform_user_id"),
        @Index(name = "uk_nx_platform_user_oauth_provider_subject",
                columnList = "provider, provider_subject", unique = true)
})
@TableName(PlatformUserOauthEntity.TABLE_NAME)
public class PlatformUserOauthEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_platform_user_oauth";

    /**
     * OAuth 身份绑定标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String identityId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "poi";
    }

    /**
     * 平台域 user 标识符。
     */
    @Column(length = 32, nullable = false)
    private String platformUserId;

    /**
     * 外部身份提供方名称。
     */
    @Column(length = 64, nullable = false)
    private String provider;

    /**
     * 提供方侧唯一主体标识符。
     */
    @Column(length = 256, nullable = false)
    private String providerSubject;

    /**
     * 提供方侧账号句柄。
     */
    @Column(length = 128)
    private String providerAccount;

    /**
     * 提供方上报的显示名称。
     */
    @Column(length = 128)
    private String providerDisplayName;

    /**
     * 身份提供方上报的邮箱地址。
     */
    @Column(length = 128)
    private String providerEmail;

    /**
     * 提供方上报的头像 URL。
     */
    @Column(length = 512)
    private String providerAvatarUrl;

    /**
     * 提供方访问令牌的存储键。
     */
    @Column(length = 256)
    private String accessTokenKey;

    /**
     * 提供方刷新令牌的存储键。
     */
    @Column(length = 256)
    private String refreshTokenKey;

    /**
     * 访问令牌过期时间。
     */
    @Column
    private LocalDateTime tokenExpiresAt;
}
