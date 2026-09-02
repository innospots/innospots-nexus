package com.innospots.nexus.quarkus.server;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Nexus 标准应用服务 Quarkus 入口。
 *
 * <p>使用 {@code application.yaml} 配置独立服务运行参数；插件策略默认值见
 * {@link com.innospots.nexus.quarkus.plugin.config.NexusPluginHostConfig}。</p>
 */
@QuarkusMain
public class NexusAppServer {

    /**
     * 启动应用服务进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
