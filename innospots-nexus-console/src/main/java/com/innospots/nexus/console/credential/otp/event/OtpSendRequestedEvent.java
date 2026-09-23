package com.innospots.nexus.console.credential.otp.event;

import com.innospots.nexus.base.events.DomainEvent;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;

/**
 * OTP 明文码投递请求；adapter 订阅后按 {@link #channel} 与 {@link #templateId} 渲染并发送。
 * <p>明文仅存在于事件载荷，禁止写入 {@code nx_otp_challenge} 或应用日志。</p>
 *
 * @param challengeId   挑战主键，与库表 {@code challenge_id} 一致，供投递侧关联与审计
 * @param purpose       业务用途，决定消息模板族
 * @param channel       下发通道，影响 {@link #eventType()} 与投递实现选择
 * @param destination   已规范化的投递地址（与库表 {@code destination} 一致）
 * @param plaintextCode 一次性数字验证码明文
 * @param templateId    稳定模板键，形如 {@code credential.otp.<purpose>.<channel>}
 * @param locale        模板语言标签，可为 null 表示默认语言
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService#issue
 * @see com.innospots.nexus.base.events.EventBus
 */
public record OtpSendRequestedEvent(
        String challengeId,
        OtpPurpose purpose,
        OtpChannel channel,
        String destination,
        String plaintextCode,
        String templateId,
        String locale
) implements DomainEvent {

    /**
     * 按通道区分的类型标识，供 {@link com.innospots.nexus.base.events.EventHandler} 过滤。
     * <p>例如邮箱为 {@code credential.otp.send.email}。</p>
     *
     * @return 小写通道后缀的事件类型名
     */
    @Override
    public String eventType() {
        return "credential.otp.send." + channel.name().toLowerCase();
    }
}
