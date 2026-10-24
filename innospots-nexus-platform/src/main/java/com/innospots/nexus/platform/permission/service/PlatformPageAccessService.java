package com.innospots.nexus.platform.permission.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifier;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
/**
 * 运营平台页面访问校验（开放 HTTP；有会话时按 catalog 授权判定）。
 */
@RequiredArgsConstructor
public class PlatformPageAccessService {

    private final PageAccessVerifier pageAccessVerifier;
    private final AuthorizationSubjectResolver subjectResolver;

    public PageAccessVerifyResult verify(PageAccessVerifyRequest request) {
        return subjectResolver.resolve()
                .map(subject -> {
                    if (ConsoleSessionContext.scope() == ConsoleSessionScope.PLATFORM) {
                        return pageAccessVerifier.verify(request, null, subject);
                    }
                    String workspaceId = SessionContext.workspaceId();
                    if (workspaceId == null || workspaceId.isBlank()) {
                        return PageAccessVerifyResult.deny("Workspace is required");
                    }
                    return pageAccessVerifier.verify(request, workspaceId, subject);
                })
                .orElseGet(() -> PageAccessVerifyResult.deny("Authentication is required"));
    }
}
