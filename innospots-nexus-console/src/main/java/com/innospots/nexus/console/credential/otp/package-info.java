/**
 * 短生命周期 OTP 子域：挑战表 {@code nx_otp_challenge}、策略 {@link com.innospots.nexus.console.credential.otp.policy.OtpPolicy}、
 * 领域服务 {@link com.innospots.nexus.console.credential.otp.service.OtpChallengeService}，以及进程内发送事件
 * {@link com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent}。
 * <p>本包不实现 SMTP/短信网关；adapter 订阅事件完成实际投递。忘记密码场景通过
 * {@link com.innospots.nexus.console.credential.otp.service.OtpPasswordVerificationOperator} 对接
 * {@link com.innospots.nexus.console.credential.password.PasswordVerificationOperator}；
 * 图形 {@code captchaCode} 见 {@link com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService}。</p>
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService
 * @see com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent
 * @see com.innospots.nexus.console.credential.password.PasswordVerificationOperator
 */
package com.innospots.nexus.console.credential.otp;
