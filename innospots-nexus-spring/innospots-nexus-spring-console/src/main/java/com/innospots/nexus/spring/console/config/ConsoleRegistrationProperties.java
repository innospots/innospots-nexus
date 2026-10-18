package com.innospots.nexus.spring.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.config.RegistrationMode;
import com.innospots.nexus.console.config.RegistrationOtpVerification;

import lombok.Getter;
import lombok.Setter;

/**
 * Spring 配置属性：绑定 {@code nexus.console.registration.*} 并映射为 {@link RegistrationConfig}。
 *
 * @author Smars
 * @date 2026/10/18
 * @see ConsoleRegistrationConfiguration
 * @see RegistrationConfig
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nexus.console.registration")
public class ConsoleRegistrationProperties {

    /** 当前自助注册模式；对应 YAML {@code mode}。 */
    private RegistrationMode mode = RegistrationMode.INVITE;

    /** 注册 OTP 校验方式；对应 YAML {@code otp-verification}。 */
    private RegistrationOtpVerification otpVerification = RegistrationOtpVerification.NONE;

    /** OTP 联调与投递参数；对应 YAML {@code otp.*}。 */
    private OtpDelivery otp = new OtpDelivery();

    /**
     * 设置 OTP 配置；{@code null} 时重置为默认空实例。
     *
     * @param otp OTP 配置节点
     */
    public void setOtp(OtpDelivery otp) {
        this.otp = otp == null ? new OtpDelivery() : otp;
    }

    /** 绑定 {@code nexus.console.registration.otp.*}。 */
    @Getter
    @Setter
    public static class OtpDelivery {

        /** 是否在 DEBUG 日志打印 OTP 明文；对应 {@code debug-log-enabled}。 */
        private boolean debugLogEnabled = false;

        /** 邮件 SMTP；对应 {@code otp.email.*}。 */
        private Email email = new Email();

        /** 短信 HTTP API；对应 {@code otp.mobile.*}。 */
        private Mobile mobile = new Mobile();

        public void setEmail(Email email) {
            this.email = email == null ? new Email() : email;
        }

        public void setMobile(Mobile mobile) {
            this.mobile = mobile == null ? new Mobile() : mobile;
        }
    }

    /** 绑定 {@code nexus.console.registration.otp.email.*}。 */
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

    /** 绑定 {@code nexus.console.registration.otp.mobile.*}。 */
    @Getter
    @Setter
    public static class Mobile {

        /** 短信网关完整请求 URL。 */
        private String apiUrl;

        /** 调用短信 API 的鉴权密钥或 Token。 */
        private String apiKey;
    }
}
