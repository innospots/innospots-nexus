package com.innospots.nexus.console.credential.otp.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.base.events.EventHandler;
import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent;

/**
 * 在 {@link RegistrationConfig.OtpDelivery#isDebugLogEnabled()} 为 true 时，
 * 将 OTP 明文、challengeId 与投递地址写入 DEBUG 日志（仅联调/本地）。
 *
 * @author Smars
 * @date 2026/10/18
 * @see OtpSendRequestedEvent
 * @see com.innospots.nexus.spring.console.config.ConsoleRegistrationConfiguration
 */
public final class RegistrationOtpDebugLogger {

    private static final Logger log = LoggerFactory.getLogger(RegistrationOtpDebugLogger.class);

    /** 订阅 {@link OtpSendRequestedEvent} 并输出调试字段。 */
    private final EventHandler<OtpSendRequestedEvent> handler = event -> {
        if (log.isDebugEnabled()) {
            log.debug(
                    "OTP delivery debug challengeId={} purpose={} channel={} destination={} templateId={} code={}",
                    event.challengeId(),
                    event.purpose(),
                    event.channel(),
                    event.destination(),
                    event.templateId(),
                    event.plaintextCode());
        }
        return null;
    };

    private RegistrationOtpDebugLogger() {
    }

    /**
     * 若配置开启则订阅 {@link OtpSendRequestedEvent}（幂等调用由装配方保证仅注册一次）。
     *
     * @param registrationConfig 注册配置；为 null 或未开启调试时无操作
     */
    public static void registerIfEnabled(RegistrationConfig registrationConfig) {
        if (registrationConfig == null || !registrationConfig.getOtp().isDebugLogEnabled()) {
            return;
        }
        EventBus.subscribe(OtpSendRequestedEvent.class, new RegistrationOtpDebugLogger().handler);
        log.info("Registration OTP debug logging enabled (plaintext codes will appear at DEBUG level)");
    }
}
