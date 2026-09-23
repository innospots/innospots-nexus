package com.innospots.nexus.sample.spring.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.sample.spring.platform.config.PlatformSampleConsoleAuthConfiguration;
import com.innospots.nexus.spring.console.EnableNexusConsole;

/**
 * 示例：运营平台控制台可执行入口。
 */
@SpringBootApplication
@EnableNexusConsole
@Import(PlatformSampleConsoleAuthConfiguration.class)
public class SamplePlatformConsoleServer {

    /**
     * 启动运营平台控制台。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SamplePlatformConsoleServer.class, args);
    }
}
