package com.innospots.nexus.console.credential.otp.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.dao.OtpChallengeDao;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent;
import com.innospots.nexus.console.credential.otp.policy.OtpPolicy;
import com.innospots.nexus.console.credential.otp.status.OtpStatusCode;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentMatchers;

class OtpChallengeServiceTest {

    private final List<OtpChallengeEntity> store = new ArrayList<>();
    private final AtomicReference<OtpSendRequestedEvent> lastEvent = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        store.clear();
        EventBus.clear();
        EventBus.subscribe(OtpSendRequestedEvent.class, event -> {
            lastEvent.set(event);
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        EventBus.clear();
    }

    @Test
    void issuePublishesSendEventWithChannel() {
        OtpChallengeService service = newService();
        service.issue(new OtpIssueCommand(
                SecurityRealm.TENANT,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.EMAIL,
                "user@example.com",
                "zh"));
        OtpSendRequestedEvent event = lastEvent.get();
        assertThat(event).isNotNull();
        assertThat(event.channel()).isEqualTo(OtpChannel.EMAIL);
        assertThat(event.destination()).isEqualTo("user@example.com");
        assertThat(event.plaintextCode()).hasSize(6);
        assertThat(event.eventType()).isEqualTo("credential.otp.send.email");
    }

    @Test
    void issuePublishesMobileEventType() {
        OtpChallengeService service = newService();
        service.issue(new OtpIssueCommand(
                SecurityRealm.TENANT,
                OtpPurpose.LOGIN_STEP_UP,
                OtpChannel.MOBILE,
                "+8613800000000",
                null));
        assertThat(lastEvent.get().channel()).isEqualTo(OtpChannel.MOBILE);
        assertThat(lastEvent.get().eventType()).isEqualTo("credential.otp.send.mobile");
    }

    @Test
    void verifyConsumesAttemptsOnFailure() {
        OtpChallengeService service = newService();
        service.issue(new OtpIssueCommand(
                SecurityRealm.TENANT,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.EMAIL,
                "user@example.com",
                "en"));
        String code = lastEvent.get().plaintextCode();

        OtpVerifyCommand bad = new OtpVerifyCommand(
                SecurityRealm.TENANT,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.EMAIL,
                "user@example.com",
                "000000");
        assertThatThrownBy(() -> service.verifyOrThrow(bad))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(OtpStatusCode.CODE_INVALID.fullCode());

        OtpVerifyCommand good = new OtpVerifyCommand(
                SecurityRealm.TENANT,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.EMAIL,
                "user@example.com",
                code);
        assertThat(store.getFirst().getAttemptCount()).isEqualTo(1);
        service.verifyOrThrow(good);
        assertThat(store.getFirst().getAttemptCount()).isEqualTo(1);
    }

    @Test
    void invalidateMarksChallengeConsumed() {
        OtpChallengeService service = newService();
        service.issue(new OtpIssueCommand(
                SecurityRealm.TENANT,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.MOBILE,
                "+86 138 0000 0000",
                null));
        service.invalidate(
                SecurityRealm.TENANT,
                OtpPurpose.PASSWORD_RESET,
                OtpChannel.MOBILE,
                "+86 138 0000 0000");
        assertThat(store.getFirst().getConsumedAt()).isNotNull();
        assertThat(store.getFirst().getDestination()).isEqualTo("+8613800000000");
    }

    @Test
    void issueStampsTenantRealmOwnership() {
        OtpChallengeDao dao = mockDao();
        OtpPolicy policy = new OtpPolicy(6, Duration.ofMinutes(10), Duration.ZERO, 3);
        OtpChallengeService service = new OtpChallengeService(dao, policy);
        service.issue(new OtpIssueCommand(
                SecurityRealm.TENANT,
                OtpPurpose.LOGIN_STEP_UP,
                OtpChannel.EMAIL,
                "a@b.com",
                null));
        OtpChallengeEntity entity = store.getFirst();
        assertThat(entity.getOwnerType()).isEqualTo(RoleOwnerType.TENANT.name());
        assertThat(entity.getSecurityRealm()).isEqualTo(SecurityRealm.TENANT.name());
        verify(dao).insert(ArgumentMatchers.<OtpChallengeEntity>any());
    }

    private OtpChallengeService newService() {
        return new OtpChallengeService(mockDao(), new OtpPolicy(6, Duration.ofMinutes(10), Duration.ZERO, 3));
    }

    private OtpChallengeDao mockDao() {
        OtpChallengeDao dao = mock(OtpChallengeDao.class);
        when(dao.insert(ArgumentMatchers.<OtpChallengeEntity>any())).thenAnswer(invocation -> {
            OtpChallengeEntity entity = invocation.getArgument(0);
            entity.setChallengeId("och-test");
            entity.setCreatedAt(LocalDateTime.now().minusMinutes(5));
            store.add(entity);
            return 1;
        });
        when(dao.selectList(any())).thenAnswer(invocation -> new ArrayList<>(store));
        when(dao.updateById(ArgumentMatchers.<OtpChallengeEntity>any())).thenAnswer(invocation -> {
            OtpChallengeEntity updated = invocation.getArgument(0);
            store.removeIf(row -> row.getChallengeId().equals(updated.getChallengeId()));
            store.add(updated);
            return 1;
        });
        return dao;
    }
}
