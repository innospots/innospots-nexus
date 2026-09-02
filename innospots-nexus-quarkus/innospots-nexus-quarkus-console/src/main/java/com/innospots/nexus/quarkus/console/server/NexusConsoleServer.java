package com.innospots.nexus.quarkus.console.server;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Nexus 管理控制台 Quarkus 入口。
 *
 * <p>classpath 包含 {@code innospots-nexus-quarkus-console} 时启动完整控制台装配。</p>
 */
@QuarkusMain
public class NexusConsoleServer {

    /**
     * 启动管理控制台进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
