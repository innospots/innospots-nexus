package com.innospots.nexus.sample.quarkus.app;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * 示例：标准应用服务 Quarkus 入口。
 */
@QuarkusMain
public class SampleQuarkusAppServer {

    /**
     * 启动示例应用服务。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
