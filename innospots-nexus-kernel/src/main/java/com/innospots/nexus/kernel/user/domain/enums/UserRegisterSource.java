package com.innospots.nexus.kernel.user.domain.enums;

/**
 * Registration source used to distinguish local password registration from
 * external identity-provider registration.
 */
public enum UserRegisterSource {

    PASSWORD,
    OAUTH
}
