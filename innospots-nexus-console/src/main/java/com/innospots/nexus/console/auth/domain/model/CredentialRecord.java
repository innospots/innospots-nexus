package com.innospots.nexus.console.auth.domain.model;

import java.time.LocalDateTime;

/**
 * Password credential snapshot for a realm user.
 *
 * @param userId             owner identifier
 * @param passwordHash       stored hash
 * @param passwordSalt       hash salt
 * @param passwordAlgorithm  algorithm name
 * @param failedAttempts     consecutive failures
 * @param lockedUntil        lock expiry
 * @param forceReset         whether next login must change password
 */
public record CredentialRecord(
        String userId,
        String passwordHash,
        String passwordSalt,
        String passwordAlgorithm,
        Integer failedAttempts,
        LocalDateTime lockedUntil,
        Boolean forceReset
) {
}
