package com.innospots.nexus.platform.invite.support;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 邀请令牌与邀请码生成。
 */
public final class PlatformInviteCredentials {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private PlatformInviteCredentials() {
    }

    /**
     * 生成 URL 令牌（32 字节十六进制）。
     */
    public static String newToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * 生成 8 位邀请码，格式 {@code XXXX-XXXX}。
     */
    public static String newInviteCode() {
        StringBuilder raw = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            raw.append(CODE_ALPHABET[RANDOM.nextInt(CODE_ALPHABET.length)]);
        }
        return raw.substring(0, 4) + "-" + raw.substring(4);
    }

    /**
     * 规范化用户输入的邀请码（去空格、大写）。
     */
    public static String normalizeInviteCode(String inviteCode) {
        if (inviteCode == null) {
            return null;
        }
        String compact = inviteCode.trim().toUpperCase().replace(" ", "").replace("-", "");
        if (compact.length() == 8) {
            return compact.substring(0, 4) + "-" + compact.substring(4);
        }
        return inviteCode.trim().toUpperCase().replace(" ", "");
    }
}
