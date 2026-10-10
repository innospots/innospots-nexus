package com.innospots.nexus.sample.spring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.core.bootstrap.EnableNexusSimpleBootstrap;

/**
 * 示例：仅启用 {@link EnableNexusSimpleBootstrap} 的轻量应用服务入口。
 */
@SpringBootApplication
@EnableNexusSimpleBootstrap
public class SampleSpringAppServer {

    /**
     * 启动示例应用服务。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SampleSpringAppServer.class, args);
    }
}
