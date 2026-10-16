package com.innospots.nexus.console.auth.domain.vo;

/**
 * 登录密码 RSA 加密用公钥（X.509 SPKI Base64）。
 *
 * @param publicKeyBase64 SPKI 公钥 Base64
 */
public record AuthRsaPublicKeyVo(String publicKeyBase64) {
}
