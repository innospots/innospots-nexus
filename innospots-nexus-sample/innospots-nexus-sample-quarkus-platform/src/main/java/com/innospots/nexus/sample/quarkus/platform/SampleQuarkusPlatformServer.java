package com.innospots.nexus.sample.quarkus.platform;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * 示例：运营平台控制台 Quarkus 入口。
 */
@QuarkusMain
public class SampleQuarkusPlatformServer {

    /**
     * 启动运营平台控制台。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
