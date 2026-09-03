package com.innospots.nexus.spring.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.plugin.config.EnableNexusPluginHost;
import com.innospots.nexus.spring.bootstrap.EnableNexusAppBootstrap;

/**
 * Nexus 标准应用服务入口。
 *
 * <p>组合启动引导与插件宿主装配；运行参数见 {@code application.yaml}。</p>
 */
@SpringBootApplication
@EnableNexusAppBootstrap
@EnableNexusPluginHost
public class NexusSpringAppServer {

    /**
     * 启动应用服务进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NexusSpringAppServer.class, args);
    }
}
