package com.innospots.nexus.spring.console.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.console.EnableNexusConsole;
import com.innospots.nexus.spring.console.bootstrap.EnableNexusConsoleBootstrap;

/**
 * Nexus 管理控制台入口。
 *
 * <p>组合控制台引导、插件宿主与 Contribution 装配，使用默认内存 H2 配置即可本地运行。</p>
 */
@SpringBootApplication
@EnableNexusConsoleBootstrap
@EnableNexusConsole
public class NexusConsoleServer {

    /**
     * 启动管理控制台进程。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NexusConsoleServer.class, args);
    }
}
