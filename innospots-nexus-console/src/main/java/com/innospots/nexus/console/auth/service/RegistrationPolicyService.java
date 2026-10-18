package com.innospots.nexus.console.auth.service;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.vo.PublicRegistrationPolicyVo;
import com.innospots.nexus.console.auth.status.RegistrationPolicyStatusCode;
import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.config.RegistrationMode;

/**
 * 基于 {@link RegistrationConfig} 的注册策略查询与公开 API 门禁。
 *
 * <p>平台公开注册服务在执行业务前应调用 {@link #requireSelfServiceMode(RegistrationMode)}，
 * 与配置不一致时抛出 {@link RegistrationPolicyStatusCode#REGISTRATION_MODE_DISABLED}。</p>
 *
 * @author Smars
 * @date 2026/10/18
 * @see PublicRegistrationPolicyVo
 * @see com.innospots.nexus.platform.access.endpoint.PlatformPublicRegistrationPolicyEndpoint
 */
public class RegistrationPolicyService {

    /** 部署期注册与 OTP 配置（不可变引用，内容由装配方注入）。 */
    private final RegistrationConfig registrationConfig;

    /**
     * @param registrationConfig 注册配置 Bean
     */
    public RegistrationPolicyService(RegistrationConfig registrationConfig) {
        this.registrationConfig = Checks.notNull(registrationConfig, "registrationConfig");
    }

    /**
     * 返回供匿名前端渲染的注册策略快照（不含 SMTP 密钥等敏感投递参数）。
     *
     * @return 当前模式、OTP 方式与各注册入口是否可用
     */
    public PublicRegistrationPolicyVo getPublicPolicy() {
        RegistrationMode mode = registrationConfig.getMode();
        return new PublicRegistrationPolicyVo(
                mode,
                registrationConfig.getOtpVerification(),
                registrationConfig.allows(RegistrationMode.OPEN),
                registrationConfig.allows(RegistrationMode.INVITE),
                registrationConfig.allows(RegistrationMode.APPROVAL));
    }

    /**
     * 自助注册流程门禁：当前 {@link RegistrationConfig#getMode()} 须与期望流程一致且未为 {@link RegistrationMode#CLOSED}。
     *
     * @param expected 业务 API 对应的注册模式（如 OPEN 开放注册）
     * @throws NexusException 模式不匹配或已关闭时 {@link RegistrationPolicyStatusCode#REGISTRATION_MODE_DISABLED}
     */
    public void requireSelfServiceMode(RegistrationMode expected) {
        Checks.notNull(expected, "expected");
        if (!registrationConfig.allows(expected)) {
            throw NexusException.build(RegistrationPolicyStatusCode.REGISTRATION_MODE_DISABLED);
        }
    }

    /**
     * 暴露底层配置供平台注册服务读取 OTP 校验方式与投递参数。
     *
     * @return 注册配置
     */
    public RegistrationConfig registrationConfig() {
        return registrationConfig;
    }
}
