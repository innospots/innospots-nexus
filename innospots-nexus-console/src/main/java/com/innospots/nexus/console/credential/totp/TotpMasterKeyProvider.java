package com.innospots.nexus.console.credential.totp;

import com.innospots.nexus.base.util.Checks;

/**
 * 提供 TOTP 共享密钥静态加密用的主密钥（应用或 adapter 注入，须为 16/24/32 字节 AES 材料）。
 *
 * @author Smars
 * @date 2026/09/19
 * @see TotpSecretProtector
 */
public interface TotpMasterKeyProvider {

    /**
     * AES-GCM 密钥材料（建议 32 字节 UTF-8 或等长随机串）。
     * <p>调用场景：{@link TotpSecretProtector} 加解密 TOTP 共享密钥时每次读取。</p>
     *
     * @return 主密钥字符串（不得记录到日志）
     */
    String masterKey();

    /**
     * 固定密钥（单测或开发环境）。
     * @param masterKey 固定主密钥
     * @return 提供者实现
     */
    static TotpMasterKeyProvider fixed(String masterKey) {
        Checks.notBlank(masterKey, "masterKey");
        return () -> masterKey;
    }
}
