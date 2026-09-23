package com.innospots.nexus.sample.spring.platform.scope;

import java.util.List;

import com.innospots.nexus.console.auth.api.MembershipDirectory;

/**
 * 运营平台示例：不依赖 kernel 的成员关系占位实现。
 */
public final class SamplePlatformScopeSupport {

    private SamplePlatformScopeSupport() {
    }

    public static MembershipDirectory emptyMembershipDirectory() {
        return tenantUserId -> List.of();
    }
}
