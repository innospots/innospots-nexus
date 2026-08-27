package com.innospots.nexus.platform.user.domain.vo;

/**
 * Platform user summary returned by ops APIs.
 *
 * @param platformUserId platform-realm user identifier
 * @param loginName      unique login name
 * @param displayName    display name
 * @param email          email address
 * @param mobile         mobile phone number
 * @param employeeNo     internal employee number
 * @param status         lifecycle status
 */
public record PlatformUserVo(
        String platformUserId,
        String loginName,
        String displayName,
        String email,
        String mobile,
        String employeeNo,
        String status
) {
}
