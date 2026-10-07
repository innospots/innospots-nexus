package com.innospots.nexus.platform.auth.support;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;

/**
 * 已登录运营管理平台会话上的 REST 辅助逻辑。
 */
public final class PlatformAuthSessionSupport {

    private PlatformAuthSessionSupport() {
    }

    /**
     * 解析当前请求绑定的平台用户 ID。
     *
     * @return 非空白平台用户 ID
     */
    public static String requirePlatformUserId() {
        String platformUserId = TLC.platformUserId();
        if (platformUserId == null || platformUserId.isBlank()) {
            platformUserId = TLC.getString(TLC.USER_ID);
        }
        if (platformUserId == null || platformUserId.isBlank()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        return platformUserId;
    }
}
