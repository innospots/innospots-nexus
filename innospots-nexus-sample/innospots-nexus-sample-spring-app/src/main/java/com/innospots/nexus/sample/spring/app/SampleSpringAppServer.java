package com.innospots.nexus.sample.spring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.bootstrap.EnableNexusAppBootstrap;
import com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost;

/**
 * 示例：标准应用服务入口。
 */
@SpringBootApplication
@EnableNexusAppBootstrap
@EnableNexusPluginHost
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
