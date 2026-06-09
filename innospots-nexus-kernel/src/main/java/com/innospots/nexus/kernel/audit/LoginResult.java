package com.innospots.nexus.kernel.audit;

/**
 * Login audit result.
 */
public enum LoginResult {

    SUCCESS,
    PASSWORD_ERROR,
    USER_NOT_FOUND,
    LOCKED,
    REJECTED
}
