package com.innospots.nexus.spring.bootstrap;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.innospots.nexus.spring.core.plugin.EnableNexusPluginHost;

/**
 * 仅用于 {@code innospots-nexus-spring-app} 模块内集成测试的启动类。
 */
@SpringBootApplication
@EnableNexusAppBootstrap
@EnableNexusPluginHost
public class NexusAppBootstrapTestApplication {
}
