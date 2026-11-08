package com.innospots.nexus.console.bootstrap.domain.config;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 初始用户清单（classpath {@code console/bootstrap/users.yaml}）。
 *
 * <p>用于在服务启动时为后台创建初始管理用户并授予角色。已同步过的用户不会再次
 * 创建（后台用户只能下线，不能删除），因此重复启动是幂等的。</p>
 *
 * <p>初始密码当前以明文配置，密码哈希在写入时生成；后续应改为外部密钥或加密载荷。
 * 通过 {@code enabled} 可以整体关闭用户初始化。</p>
 *
 * @param enabled 是否执行用户初始化；{@code false} 时跳过整个清单
 * @param users   初始用户列表
 */
@Schema(name = "InitialUserManifest", description = "初始用户清单")
public record InitialUserManifest(Boolean enabled, List<InitialUser> users) {

    /**
     * 是否启用用户初始化；缺省视为启用。
     *
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    /**
     * 初始用户。
     *
     * @param loginName   登录名，作为同步去重的稳定标识
     * @param displayName 显示名称；为空时回退 {@code loginName}
     * @param email       邮箱
     * @param mobile      手机号
     * @param password    初始明文密码；仅在首次创建时使用
     * @param securityRealm 归属安全域：{@code PLATFORM} / {@code TENANT}
     * @param roleCodes   初始角色编码列表，对应 roles.yaml 中的 {@code roleCode}
     */
    @Schema(name = "InitialUser", description = "初始用户")
    public record InitialUser(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String password,
            String securityRealm,
            List<String> roleCodes
    ) {
    }
}
