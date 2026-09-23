package com.innospots.nexus.spring.service.bootstrap;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.transfer.config.ServiceTransferModuleConfiguration;

/**
 * 启用 Nexus 文件传输写回：{@code DownloadResource} 返回值处理器（MVC / WebFlux 按条件生效）。
 *
 * <p>HTTP 入口仍须 {@link EnableNexusServiceHttp}。</p>
 *
 * @author Smars
 * @date 2026/09/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        ServiceTransferModuleConfiguration.ServletTransferModuleConfiguration.class,
        ServiceTransferModuleConfiguration.ReactiveTransferModuleConfiguration.class
})
public @interface EnableNexusServiceTransfer {
}
