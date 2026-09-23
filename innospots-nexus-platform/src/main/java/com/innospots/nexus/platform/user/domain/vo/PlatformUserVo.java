package com.innospots.nexus.platform.user.domain.vo;

/**
 * 运维 API 返回的平台用户概要。
 *
 * @author Smars
 * @date 2026/09/13
 * @param platformUserId platform-realm user 标识符
 * @param loginName      唯一登录名
 * @param displayName    显示名称
 * @param email          邮箱地址
 * @param mobile         手机号
 * @param employeeNo     内部员工编号
 * @param status         生命周期状态
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
