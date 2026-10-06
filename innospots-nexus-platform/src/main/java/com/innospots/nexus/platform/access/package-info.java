/**
 * 平台自助注册中的「注册待审核」（APPROVAL）流与公开/管理端 REST 边界。
 *
 * <p>APPROVAL 模式下，用户提交注册即创建 {@code PENDING_APPROVAL} 平台用户并写入
 * {@code nx_pl_access_request}；审批通过后直接 {@code ACTIVE}，无需二次发邀请。
 * OPEN 模式见 {@link com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService}。</p>
 *
 * <p>分层约定：{@code operator} 仅访问本表 DAO；{@code service} 编排 OTP、用户与策略。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.endpoint.PlatformPublicAccessRegistrationEndpoint
 * @see com.innospots.nexus.platform.access.endpoint.PlatformAccessRequestEndpoint
 */
package com.innospots.nexus.platform.access;
