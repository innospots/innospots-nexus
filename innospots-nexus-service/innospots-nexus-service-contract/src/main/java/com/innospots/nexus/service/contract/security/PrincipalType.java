package com.innospots.nexus.service.contract.security;

/**
 * 已认证调用方类型。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServicePrincipal
 */
public enum PrincipalType {
    USER,
    APPLICATION,
    SERVICE,
    API_KEY,
    ANONYMOUS
}
