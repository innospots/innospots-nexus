package com.innospots.nexus.console.auth.domain.request;

/**
 * Authenticated password change.
 *
 * @param oldEncryptedPassword current frontend encrypted password
 * @param newEncryptedPassword desired frontend encrypted password
 */
public record PasswordChangeRequest(String oldEncryptedPassword, String newEncryptedPassword) {
}
