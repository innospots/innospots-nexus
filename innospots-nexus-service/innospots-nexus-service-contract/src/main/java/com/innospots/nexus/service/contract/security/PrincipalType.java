package com.innospots.nexus.service.contract.security;

/**
 * Kind of authenticated caller.
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
