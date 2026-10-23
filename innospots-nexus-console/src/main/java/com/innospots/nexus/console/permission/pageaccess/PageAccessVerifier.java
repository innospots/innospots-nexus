package com.innospots.nexus.console.permission.pageaccess;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;

/**
 * 按 catalog 页面入口与权限分组校验页面访问（与 HTTP Filter 解耦）。
 */
public interface PageAccessVerifier {

    /**
     * @param workspaceId 当前工作区（授权记录归属）
     * @param subject     当前登录主体
     */
    PageAccessVerifyResult verify(
            PageAccessVerifyRequest request,
            String workspaceId,
            AuthorizationSubject subject
    );
}
