package com.innospots.nexus.kernel.user;

/**
 * Kernel user aggregate root for identity management.
 *
 * @param userId      user identifier
 * @param account     login account
 * @param displayName display name
 * @param email       email address
 * @param status      lifecycle status
 */
public record KernelUser(
        String userId,
        String account,
        String displayName,
        String email,
        UserStatus status
) {
}
