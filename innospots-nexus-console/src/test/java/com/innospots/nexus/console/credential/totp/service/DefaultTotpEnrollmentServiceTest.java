package com.innospots.nexus.console.credential.totp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithmRegistry;
import com.innospots.nexus.console.credential.password.dao.UserCredentialDao;
import com.innospots.nexus.console.credential.password.domain.entity.UserCredentialEntity;
import com.innospots.nexus.console.credential.password.operator.UserCredentialOperator;
import com.innospots.nexus.console.credential.totp.TotpCredentials;
import com.innospots.nexus.console.credential.totp.TotpMasterKeyProvider;
import com.innospots.nexus.console.credential.totp.TotpSecretProtector;
import com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithm;
import com.innospots.nexus.console.credential.totp.domain.TotpEnrollmentMaterial;
import com.innospots.nexus.console.credential.totp.status.TotpStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

class DefaultTotpEnrollmentServiceTest {

    private static final String MASTER_KEY = "01234567890123456789012345678901";

    private final Map<String, UserCredentialEntity> store = new HashMap<>();
    private UserCredentialOperator credentialOperator;
    private DefaultTotpEnrollmentService enrollmentService;
    private TotpSecretProtector secretProtector;

    @BeforeEach
    void setUp() {
        store.clear();
        UserCredentialDao dao = mock(UserCredentialDao.class);
        when(dao.insert(ArgumentMatchers.<UserCredentialEntity>any())).thenAnswer(invocation -> {
            UserCredentialEntity entity = invocation.getArgument(0);
            entity.setCredentialId("ucr-totp-" + entity.getSubjectId());
            store.put(entity.getSubjectId(), entity);
            return 1;
        });
        when(dao.updateById(ArgumentMatchers.<UserCredentialEntity>any())).thenAnswer(invocation -> {
            UserCredentialEntity entity = invocation.getArgument(0);
            store.put(entity.getSubjectId(), entity);
            return 1;
        });
        when(dao.selectOne(any())).thenAnswer(invocation -> {
            if (store.isEmpty()) {
                return null;
            }
            return store.values().iterator().next();
        });
        when(dao.deleteById(ArgumentMatchers.<String>any())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            store.values().removeIf(entity -> entity.getCredentialId().equals(id));
            return 1;
        });
        credentialOperator = new UserCredentialOperator(dao, CredentialAlgorithmRegistry.defaults());
        secretProtector = new TotpSecretProtector(TotpMasterKeyProvider.fixed(MASTER_KEY));
        enrollmentService = new DefaultTotpEnrollmentService(credentialOperator, secretProtector);
    }

    @Test
    void beginAndConfirmEnrollment() {
        TotpEnrollmentMaterial material = enrollmentService.beginEnrollment(
                SecurityRealm.TENANT,
                "usr-1",
                "user@example.com");
        assertThat(material.base32Secret()).isNotBlank();
        assertThat(material.otpauthUri()).contains("otpauth://totp/");

        TotpCredentialAlgorithm algorithm = new TotpCredentialAlgorithm();
        byte[] secret = secretProtector.decodeBase32Secret(material.base32Secret());
        String code = algorithm.generateAt(secret, java.time.Instant.now());

        enrollmentService.confirmEnrollment(SecurityRealm.TENANT, "usr-1", code);
        Optional<CredentialRecord> active = credentialOperator.findCredential(
                SecurityRealm.TENANT,
                com.innospots.nexus.console.credential.password.CredentialKind.TOTP,
                "usr-1");
        assertThat(active).isPresent();
        assertThat(TotpCredentials.isEnrollmentPending(active.get())).isFalse();
    }

    @Test
    void confirmRejectsWhenNotPending() {
        TotpEnrollmentMaterial material = enrollmentService.beginEnrollment(
                SecurityRealm.TENANT,
                "usr-2",
                "b@example.com");
        byte[] secret = secretProtector.decodeBase32Secret(material.base32Secret());
        String code = new TotpCredentialAlgorithm().generateAt(secret, java.time.Instant.now());
        enrollmentService.confirmEnrollment(SecurityRealm.TENANT, "usr-2", code);

        assertThatThrownBy(() -> enrollmentService.confirmEnrollment(SecurityRealm.TENANT, "usr-2", code))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(TotpStatusCode.ALREADY_ENROLLED.fullCode());
    }
}
