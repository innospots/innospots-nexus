package com.innospots.nexus.console.credential.password.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.password.CredentialKind;
import com.innospots.nexus.console.credential.password.PasswordValidator;
import com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithms;
import com.innospots.nexus.console.credential.password.operator.UserCredentialOperator;
import com.innospots.nexus.console.credential.password.policy.LoginLockPolicy;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CredentialServiceTest {

    @Test
    void authenticateRejectsWrongPasswordAndIncrementsFailures() {
        UserCredentialOperator operator = mock(UserCredentialOperator.class);
        CredentialRecord record = passwordRecord("user-1", "Secret123", 0, null);
        when(operator.findPassword(SecurityRealm.TENANT, "user-1")).thenReturn(Optional.of(record));
        when(operator.verify(record, "wrong")).thenReturn(false);
        CredentialService service = new CredentialService(operator, new PasswordValidator(), new LoginLockPolicy(3, 10));

        assertThatThrownBy(() -> service.authenticate(SecurityRealm.TENANT, "user-1", "wrong"))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());

        verify(operator).saveState(eq(SecurityRealm.TENANT), any(CredentialRecord.class));
    }

    @Test
    void authenticateClearsFailuresOnSuccess() {
        UserCredentialOperator operator = mock(UserCredentialOperator.class);
        CredentialRecord record = passwordRecord("user-1", "Secret123", 2, null);
        when(operator.findPassword(SecurityRealm.TENANT, "user-1")).thenReturn(Optional.of(record));
        when(operator.verify(record, "Secret123")).thenReturn(true);
        CredentialService service = new CredentialService(operator, new PasswordValidator(), LoginLockPolicy.DEFAULT);

        service.authenticate(SecurityRealm.TENANT, "user-1", "Secret123");

        verify(operator).saveState(SecurityRealm.TENANT, new CredentialRecord(
                record.subjectId(),
                record.credentialKind(),
                record.algorithm(),
                record.verifier(),
                record.verifierParams(),
                record.credentialVersion(),
                0,
                null,
                record.forceReset(),
                record.expiredAt()));
    }

    private static CredentialRecord passwordRecord(
            String subjectId,
            String rawPassword,
            int failedAttempts,
            LocalDateTime lockedUntil
    ) {
        return new CredentialRecord(
                subjectId,
                CredentialKind.PASSWORD.name(),
                CredentialAlgorithms.BCRYPT_V1,
                CryptoUtils.encryptPassword(rawPassword),
                null,
                1,
                failedAttempts,
                lockedUntil,
                false,
                null);
    }
}
