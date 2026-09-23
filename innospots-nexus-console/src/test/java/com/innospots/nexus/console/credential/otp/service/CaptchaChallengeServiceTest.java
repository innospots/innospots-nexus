package com.innospots.nexus.console.credential.otp.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.captcha.CaptchaPolicy;
import com.innospots.nexus.console.credential.otp.captcha.HutoolCaptchaFactory;
import com.innospots.nexus.console.credential.otp.dao.OtpChallengeDao;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueResult;
import com.innospots.nexus.console.credential.otp.domain.CaptchaVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent;
import com.innospots.nexus.console.credential.otp.policy.OtpPolicy;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaptchaChallengeServiceTest {

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
    void issueReturnsImageWithoutOtpSendEvent() {
        CaptchaChallengeService captcha = new CaptchaChallengeService(newService());
        CaptchaIssueResult result = captcha.issue(new CaptchaIssueCommand(
                SecurityRealm.TENANT,
                OtpPurpose.CAPTCHA,
                "login-txn-1",
                CaptchaPolicy.DEFAULT));
        assertThat(result.challengeId()).isNotBlank();
        assertThat(result.imageBase64()).isNotBlank();
        assertThat(result.imageMimeType()).isEqualTo("image/png");
        assertThat(result.expiresAt()).isNotNull();
        assertThat(lastEvent.get()).isNull();
    }

    @Test
    void verifyAcceptsCaptchaCodeCaseInsensitive() {
        OtpChallengeService otp = newService();
        CaptchaChallengeService captcha = new CaptchaChallengeService(otp);
        String clientKey = "login-txn-2";
        HutoolCaptchaFactory.GeneratedCaptcha generated = HutoolCaptchaFactory.generate(CaptchaPolicy.DEFAULT);
        otp.issueCaptchaCode(
                new com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand(
                        SecurityRealm.TENANT,
                        OtpPurpose.CAPTCHA,
                        com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel.CAPTCHA,
                        clientKey,
                        null),
                generated.code());
        boolean ok = captcha.verify(new CaptchaVerifyCommand(
                SecurityRealm.TENANT,
                OtpPurpose.CAPTCHA,
                clientKey,
                generated.code().toLowerCase()));
        assertThat(ok).isTrue();
    }

    private OtpChallengeService newService() {
        OtpChallengeDao dao = mock(OtpChallengeDao.class);
        when(dao.insert(ArgumentMatchers.<OtpChallengeEntity>any())).thenAnswer(invocation -> {
            OtpChallengeEntity entity = invocation.getArgument(0);
            if (entity.getChallengeId() == null) {
                entity.setChallengeId("och-test-1");
            }
            entity.setOwnerType(RoleOwnerType.PLATFORM.name());
            entity.setOwnerId("own-1");
            entity.setSecurityRealm(SecurityRealm.TENANT.name());
            store.add(entity);
            return 1;
        });
        when(dao.selectList(any())).thenAnswer(invocation -> store.stream()
                .filter(row -> row.getConsumedAt() == null)
                .toList());
        when(dao.selectById(any())).thenAnswer(invocation -> {
            String id = invocation.getArgument(0);
            return store.stream().filter(row -> id.equals(row.getChallengeId())).findFirst().orElse(null);
        });
        when(dao.updateById(ArgumentMatchers.<OtpChallengeEntity>any())).thenAnswer(invocation -> {
            OtpChallengeEntity updated = invocation.getArgument(0);
            store.removeIf(row -> row.getChallengeId().equals(updated.getChallengeId()));
            store.add(updated);
            return 1;
        });
        OtpPolicy policy = new OtpPolicy(6, Duration.ofMinutes(5), Duration.ZERO, 5);
        return new OtpChallengeService(dao, policy);
    }
}
