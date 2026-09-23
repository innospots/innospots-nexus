package com.innospots.nexus.sample.spring.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.sample.spring.tenant.config.TenantSampleConsoleAuthConfiguration;
import com.innospots.nexus.spring.console.EnableNexusConsole;

/**
 * 示例：租户管理控制台可执行入口。
 */
@SpringBootApplication
@EnableNexusConsole
@Import(TenantSampleConsoleAuthConfiguration.class)
public class SampleTenantConsoleServer {

    /**
     * 启动租户管理控制台。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SampleTenantConsoleServer.class, args);
    }
}
