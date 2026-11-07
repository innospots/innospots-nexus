package com.innospots.nexus.console.jaxrs.support;

import java.time.Instant;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.scope.domain.session.ConsoleSessionContext;

/**
 * 将访问令牌声明绑定到 {@link SessionContext} 与 {@link TLC}。
 *
 * <p>令牌只携带身份与安全域，绑定内容为：</p>
 * <ul>
 *   <li>{@code TLC.userId}：令牌主体标识，无论哪个安全域都绑定；</li>
 *   <li>{@code TLC.platformUserId}：仅 {@link SecurityRealm#PLATFORM} 时绑定，
 *       避免租户域用户被当作平台用户；</li>
 *   <li>安全域、租户成员与租户快照。</li>
 * </ul>
 *
 * <p>工作区与项目不在令牌中，由请求期的
 * {@link com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeSessionFilter}
 * 依据客户端状态补充。</p>
 */
public final class ConsoleTokenSessionBinder {

    private ConsoleTokenSessionBinder() {
    }

    /**
     * 校验并绑定访问令牌。
     *
     * @param claims 解析后的令牌声明
     * @throws NexusException 令牌无效、过期或用途不符时
     */
    public static void bindAccessToken(TokenClaims claims) {
        if (claims == null) {
            throw ConsoleExceptions.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        if (!TokenIssuer.PURPOSE_ACCESS.equals(claims.purpose())) {
            throw ConsoleExceptions.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        if (claims.expiresAt() <= Instant.now().getEpochSecond()) {
            throw ConsoleExceptions.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        String userId = claims.userId();
        if (userId == null || userId.isBlank()) {
            throw ConsoleExceptions.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        // bindUser 会同步 TLC.USER_ID 与 TLC.USER_NAME；这里只需补安全域相关键。
        SessionContext.bindUser(UserSnapshot.simple(userId, userId, null));
        if (claims.realm() != null) {
            TLC.securityRealm(claims.realm().name());
        }
        // 仅运营平台域写入 platformUserId，避免租户域用户被当作平台用户。
        if (claims.realm() == SecurityRealm.PLATFORM) {
            TLC.platformUserId(userId);
        }
        if (claims.tenantMemberId() != null && !claims.tenantMemberId().isBlank()) {
            TLC.tenantMemberId(claims.tenantMemberId());
        }
        if (claims.tenantId() != null && !claims.tenantId().isBlank()) {
            SessionContext.bindTenant(
                    new TenantSnapshot(
                            claims.tenantId(),
                            claims.tenantId(),
                            claims.tenantId(),
                            BasicStatus.ENABLED),
                    new OrganizationSnapshot(
                            claims.tenantId(),
                            claims.tenantId(),
                            claims.tenantId(),
                            null,
                            null,
                            null,
                            BasicStatus.ENABLED));
        }
        ConsoleSessionContext.bindScope(ConsoleSessionContext.inferFromSession());
    }

    /**
     * 清除全部会话绑定。
     */
    public static void clear() {
        SessionContext.clearUser();
        SessionContext.clearTenant();
        SessionContext.clearWorkspace();
        SessionContext.clearProject();
        TLC.clear();
    }
}
