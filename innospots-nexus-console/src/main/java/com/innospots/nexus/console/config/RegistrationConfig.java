package com.innospots.nexus.console.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 平台自助注册方式与注册 OTP 投递的部署期配置（framework-neutral）。
 *
 * <p>与库表 {@code platform.registration.mode} 的管理端设置独立：公开注册门禁与 UI 策略以本配置为准。
 * Spring 绑定前缀为 {@code nexus.console.registration.*}。</p>
 *
 * @author Smars
 * @date 2026/10/18
 * @see RegistrationPolicyService
 * @see com.innospots.nexus.spring.console.config.ConsoleRegistrationProperties
 */
@Getter
@Setter
public class RegistrationConfig {

    /**
     * 当前启用的自助注册模式（互斥）；{@link RegistrationMode#CLOSED} 时所有公开注册入口关闭。
     */
    private RegistrationMode mode = RegistrationMode.INVITE;

    /**
     * 注册提交前是否要求邮箱/手机 OTP；{@link RegistrationOtpVerification#NONE} 时仅需登录名与密码。
     */
    private RegistrationOtpVerification otpVerification = RegistrationOtpVerification.NONE;

    /**
     * OTP 联调日志与邮件/短信投递参数（由后续 adapter 消费，不含业务逻辑）。
     */
    private OtpDelivery otp = new OtpDelivery();

    /**
     * 设置 OTP 投递配置；{@code null} 时重置为默认空实例。
     *
     * @param otp OTP 投递配置
     */
    public void setOtp(OtpDelivery otp) {
        this.otp = otp == null ? new OtpDelivery() : otp;
    }

    /**
     * 是否允许访问指定自助注册流程（与 {@link #mode} 一致时为 true，{@link RegistrationMode#CLOSED} 恒为 false）。
     *
     * @param flow 期望的注册流程
     * @return 配置允许该流程时为 true
     */
    public boolean allows(RegistrationMode flow) {
        if (mode == RegistrationMode.CLOSED) {
            return false;
        }
        return mode == flow;
    }

    /**
     * 注册是否需要 OTP 校验。
     *
     * @return {@link RegistrationOtpVerification#NONE} 时为 false
     */
    public boolean isOtpRequired() {
        return otpVerification != RegistrationOtpVerification.NONE;
    }

    /**
     * OTP 发送与联调相关配置。
     */
    @Getter
    @Setter
    public static class OtpDelivery {

        /**
         * 为 true 时通过 {@link com.innospots.nexus.console.credential.otp.support.RegistrationOtpDebugLogger}
         * 在 INFO 日志输出 challengeId、投递地址与明文验证码（仅本地/联调，禁止生产长期开启）。
         */
        private boolean debugLogEnabled = false;

        /** 邮件通道 SMTP 参数。 */
        private Email email = new Email();

        /** 短信通道 HTTP API 参数。 */
        private Mobile mobile = new Mobile();

        /**
         * 设置邮件配置；{@code null} 时重置为默认空实例。
         *
         * @param email 邮件 SMTP 配置
         */
        public void setEmail(Email email) {
            this.email = email == null ? new Email() : email;
        }

        /**
         * 设置短信配置；{@code null} 时重置为默认空实例。
         *
         * @param mobile 短信 API 配置
         */
        public void setMobile(Mobile mobile) {
            this.mobile = mobile == null ? new Mobile() : mobile;
        }
    }

    /**
     * 邮件 OTP 标准 SMTP 配置（供投递 adapter 读取）。
     */
    @Getter
    @Setter
    public static class Email {

        /** SMTP 主机名。 */
        private String host;

        /** SMTP 端口；默认 587。 */
        private int port = 587;

        /** SMTP 认证用户名。 */
        private String username;

        /** SMTP 认证密码或应用专用密钥。 */
        private String password;

        /** 发件人邮箱地址（From）。 */
        private String from;

        /** 发件人显示名称。 */
        private String fromName;

        /** 是否启用 STARTTLS；默认 true。 */
        private boolean tls = true;
    }

    /**
     * 短信 OTP 独立 HTTP API 配置（供投递 adapter 读取）。
     */
    @Getter
    @Setter
    public static class Mobile {

        /** 短信网关完整请求 URL（由 adapter 定义请求体契约）。 */
        private String apiUrl;

        /** 调用短信 API 的鉴权密钥或 Token。 */
        private String apiKey;
    }
}
