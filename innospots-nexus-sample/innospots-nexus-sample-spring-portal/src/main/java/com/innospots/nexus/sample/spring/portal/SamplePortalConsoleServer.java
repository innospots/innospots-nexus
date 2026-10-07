package com.innospots.nexus.sample.spring.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.portal.EnableNexusPortal;

/**
 * 示例：portal 管理控制台可执行入口。
 */
@SpringBootApplication
@EnableNexusPortal
public class SamplePortalConsoleServer {

    /**
     * 启动 portal 管理控制台。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SamplePortalConsoleServer.class, args);
    }
}
