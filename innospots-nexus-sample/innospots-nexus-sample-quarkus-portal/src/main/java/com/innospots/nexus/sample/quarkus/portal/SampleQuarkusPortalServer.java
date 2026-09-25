package com.innospots.nexus.sample.quarkus.portal;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * 示例：portal 管理控制台 Quarkus 入口。
 */
@QuarkusMain
public class SampleQuarkusPortalServer {

    /**
     * 启动 portal 管理控制台。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        Quarkus.run(args);
    }
}
