/**
 * {@code nx_user_credential} 垂直切片：实体、DAO、operator、算法、密码生命周期，以及 portal/platform 消费的
 * {@link com.innospots.nexus.console.credential.password.PasswordDecryptor}、
 * {@link com.innospots.nexus.console.credential.password.PasswordVerificationOperator}。
 * <p>{@link CredentialKind#TOTP} 行由 {@code totp} 子包编排。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.domain.entity.UserCredentialEntity
 */
package com.innospots.nexus.console.credential.password;
