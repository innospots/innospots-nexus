package com.innospots.nexus.console.auth.domain.request;

/**
 * Password login payload shared by both realms.
 *
 * @param login              user_name, email, or mobile
 * @param encryptedPassword  frontend encrypted password
 */
public record AuthLoginRequest(String login, String encryptedPassword) {
}
