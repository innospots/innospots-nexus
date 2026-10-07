package com.innospots.nexus.console.auth.domain.model;

import java.time.LocalDateTime;

/**
 * 域用户的鉴权凭据快照（opaque verifier，由 {@code algorithm} 解释）。
 *
 * @param subjectId         用户主体 ID（platform_user_id 或 tenant_user_id）
 * @param credentialKind    凭据类型 name（如 PASSWORD）
 * @param algorithm         可插拔算法 ID
 * @param verifier          不透明验证材料
 * @param verifierParams    算法私有参数（console 不解析）
 * @param credentialVersion 凭据版本
 * @param failedAttempts    连续失败次数
 * @param lockedUntil       锁定截止时间
 * @param forceReset        下次登录是否必须改密
 * @param expiredAt         过期时间
 */
public record CredentialRecord(
        String subjectId,
        String credentialKind,
        String algorithm,
        String verifier,
        String verifierParams,
        Integer credentialVersion,
        Integer failedAttempts,
        LocalDateTime lockedUntil,
        Boolean forceReset,
        LocalDateTime expiredAt
) {
}
