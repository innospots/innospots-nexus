package com.innospots.nexus.console.jaxrs.web;

import lombok.Getter;
import lombok.Setter;

/**
 * 关闭请求侧安全时的固定会话快照配置。
 */
@Getter
@Setter
public class ConsoleWebDevSessionSettings {

    /** 是否注入 dev 会话；默认 {@code false}。 */
    private boolean enabled = false;

    /** 逻辑用户 ID（字符串，与令牌声明一致）。 */
    private String userId = "1";

    /** 租户 ID。 */
    private String tenantId = "dev-tenant";

    /** 工作区 ID。 */
    private String workspaceId = "dev-workspace";

    /** 可选项目 ID。 */
    private String projectId;

    /** 可选 {@link com.innospots.nexus.base.thread.TLC#SECURITY_REALM}。 */
    private String realm;

    /** 可选租户成员 ID（{@link com.innospots.nexus.base.thread.TLC#tenantMemberId}）。 */
    private String tenantMemberId;
}
