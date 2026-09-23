package com.innospots.nexus.kernel.user.domain.enums;

/**
 * 用于区分本地密码注册与
 * 外部身份提供方注册。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum UserRegisterSource {

    /**
     * 使用本地密码凭证注册。
     */
    PASSWORD,

    /**
     * 通过外部身份提供方注册。
     */
    OAUTH
}
