package com.innospots.nexus.console.catalog.domain.model;

/**
 * console@2 {@code operationGroups.group} 键，与插件 YAML 及 {@code @EndpointGrant#group()} 对齐。
 */
public final class CatalogOperationGroups {

    public static final String DEFAULT = "default";

    public static final String CREATE = "create";

    public static final String UPDATE = "update";

    public static final String DELETE = "delete";

    public static final String STATUS = "status";

    public static final String EXECUTE = "execute";

    public static final String REPLACE = "replace";

    public static final String ASSIGN = "assign";

    public static final String REVOKE = "revoke";

    public static final String IMPORT = "import";

    public static final String WRITE = "write";

    private CatalogOperationGroups() {
    }
}
