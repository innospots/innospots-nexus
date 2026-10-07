package com.innospots.nexus.console.credential.totp.domain;

/**
 * TOTP 注册阶段返回给客户端的材料；{@code base32Secret} 与 {@code otpauthUri} 仅应在受信通道展示一次。
 *
 * @param base32Secret 共享密钥明文（库中仅存加密形式）
 * @param otpauthUri   供扫码的 provisioning URI
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.service.TotpEnrollmentService#beginEnrollment
 */
public record TotpEnrollmentMaterial(
        String base32Secret,
        String otpauthUri
) {
}
