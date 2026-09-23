package com.innospots.nexus.spring.service.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.service.bootstrap.EnableNexusService;

/**
 * WebFlux adapter 黑盒测试宿主。
 */
@SpringBootApplication
@EnableNexusService
public class WebFluxTestApplication {

    /**
     * 启动测试应用。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(WebFluxTestApplication.class, args);
    }
}
