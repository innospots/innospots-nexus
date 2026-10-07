package com.innospots.nexus.quarkus.server;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * {@link NexusAppServerContextTest} 使用的嵌入式 Quarkus 入口。
 */
@QuarkusMain
public class NexusAppServerTestLauncher {

    /**
     * 启动测试用 Quarkus 进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
