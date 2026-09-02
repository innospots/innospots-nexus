package com.innospots.nexus.spring.bootstrap;

/**
 * Nexus 启动引导默认值常量。
 *
 * <p>框架与插件策略默认值在代码中定义；独立服务运行参数（数据源、端口、DDL）
 * 在 {@code application.yaml} 中配置，并应与此处常量保持一致。</p>
 */
public final class NexusBootstrapDefaults {

    /** 应用服务名称。 */
    public static final String APP_NAME = "nexus-app";

    /** 应用服务 HTTP 端口。 */
    public static final int APP_SERVER_PORT = 8080;

    /** 应用服务内存库名称。 */
    public static final String APP_DATABASE_NAME = "nexus";

    /** 控制台服务名称。 */
    public static final String CONSOLE_NAME = "nexus-console";

    /** 控制台 HTTP 端口。 */
    public static final int CONSOLE_SERVER_PORT = 8081;

    /** 控制台内存库名称。 */
    public static final String CONSOLE_DATABASE_NAME = "nexus-console";

    /** H2 驱动类名。 */
    public static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";

    /** H2 默认用户名。 */
    public static final String H2_USERNAME = "sa";

    /** H2 默认密码。 */
    public static final String H2_PASSWORD = "";

    /** 插件安装表 DDL 资源路径。 */
    public static final String PLUGIN_INSTALLATION_SCHEMA = "classpath:nexus/schema/plugin-installation-h2.sql";

    private NexusBootstrapDefaults() {
    }

    /**
     * 生成 H2 PostgreSQL 兼容模式的 JDBC URL。
     *
     * @param databaseName 内存库名称
     * @return JDBC URL
     */
    public static String h2JdbcUrl(String databaseName) {
        return "jdbc:h2:mem:" + databaseName
                + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    }
}
