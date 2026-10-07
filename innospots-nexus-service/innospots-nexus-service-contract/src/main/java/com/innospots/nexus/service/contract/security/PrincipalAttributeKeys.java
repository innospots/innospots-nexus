package com.innospots.nexus.service.contract.security;

/**
 * {@link ServicePrincipal#attributes()} 中用于横切能力的标准键名。
 */
public final class PrincipalAttributeKeys {

    /**
     * 业务客户标识，用于分布式限流维度 {@code customer:{id}}。
     * 由认证层在建立 {@link ServicePrincipal} 时写入。
     */
    public static final String CUSTOMER_ID = "customerId";

    private PrincipalAttributeKeys() {
    }
}
