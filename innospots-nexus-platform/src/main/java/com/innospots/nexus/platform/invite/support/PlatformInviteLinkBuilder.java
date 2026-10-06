package com.innospots.nexus.platform.invite.support;

import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * 根据部署配置拼接邀请链接。
 */
public final class PlatformInviteLinkBuilder {

    private final String publicBaseUrl;

    public PlatformInviteLinkBuilder(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl == null ? "" : publicBaseUrl.trim();
    }

    /**
     * 返回可复制的邀请链接。未配置基址时返回 API 相对路径。
     */
    public String buildInviteLink(String inviteToken) {
        String apiPath = PlatformConstant.PUBLIC_INVITES_PATH + "/" + inviteToken;
        if (publicBaseUrl.isEmpty()) {
            return apiPath;
        }
        if (publicBaseUrl.endsWith("/")) {
            return publicBaseUrl.substring(0, publicBaseUrl.length() - 1) + apiPath;
        }
        return publicBaseUrl + apiPath;
    }
}
