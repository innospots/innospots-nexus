package com.innospots.nexus.console.permission.pageaccess;

import com.innospots.nexus.console.permission.authorization.AuthorizationDecision;
import com.innospots.nexus.console.permission.authorization.AuthorizationRequest;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;

/**
 * 基于 catalog MENU 授权与 {@code grantGroup} 的默认页面访问校验实现。
 */
public final class GrantGroupPageAccessVerifier implements PageAccessVerifier {

    private static final String DEFAULT_GRANT_GROUP = "default";

    private final ConsolePagePermissionAuthorizer pagePermissionAuthorizer;

    public GrantGroupPageAccessVerifier(ConsolePagePermissionAuthorizer pagePermissionAuthorizer) {
        if (pagePermissionAuthorizer == null) {
            throw new IllegalArgumentException("pagePermissionAuthorizer is required");
        }
        this.pagePermissionAuthorizer = pagePermissionAuthorizer;
    }

    @Override
    public PageAccessVerifyResult verify(
            PageAccessVerifyRequest request,
            String workspaceId,
            AuthorizationSubject subject
    ) {
        if (request == null || blank(request.domainKey()) || blank(request.moduleKey())
                || blank(request.pageKey())) {
            return PageAccessVerifyResult.deny("Invalid page access request");
        }
        if (subject == null) {
            return PageAccessVerifyResult.deny("Authentication is required");
        }
        String grantGroup = blank(request.grantGroup()) ? DEFAULT_GRANT_GROUP : request.grantGroup().trim();
        if (!DEFAULT_GRANT_GROUP.equals(grantGroup)) {
            return PageAccessVerifyResult.deny("Grant group authorization is not implemented: " + grantGroup);
        }
        AuthorizationDecision decision = pagePermissionAuthorizer.authorize(new AuthorizationRequest(
                workspaceId,
                "GET",
                null,
                request.pageKey().trim(),
                subject));
        if (decision.allowed()) {
            return PageAccessVerifyResult.allow();
        }
        return PageAccessVerifyResult.deny(decision.denyReason());
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
