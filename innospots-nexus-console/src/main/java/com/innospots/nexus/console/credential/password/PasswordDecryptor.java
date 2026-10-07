package com.innospots.nexus.console.credential.password;

/**
 * 解密前端客户端提交的密码密文；由 portal/platform/auth 注入，实现见 {@link RsaPasswordDecryptor}。
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface PasswordDecryptor {

    /**
     * 将前端加密密码解密为原始密码字符串。
     *
     * @param encryptedPassword 前端加密密码载荷
     * @return 供服务端哈希的原始密码字符串
     */
    String decrypt(String encryptedPassword);
}
