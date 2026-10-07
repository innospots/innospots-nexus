package com.innospots.nexus.spring.service.transfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.service.transfer.download.DefaultDownloadPlanner;
import com.innospots.nexus.service.transfer.download.DownloadTransferSupport;
import com.innospots.nexus.spring.service.transfer.webflux.ReactiveDownloadWriter;
import com.innospots.nexus.spring.service.transfer.mvc.ServletDownloadWriter;

/**
 * 传输写回 Bean。
 */
@Configuration
public class ServiceTransferConfiguration {

    @Bean
    DownloadTransferSupport serviceDownloadTransferSupport() {
        return new DownloadTransferSupport(new DefaultDownloadPlanner());
    }

    @Bean
    ServletDownloadWriter serviceServletDownloadWriter(DownloadTransferSupport serviceDownloadTransferSupport) {
        return new ServletDownloadWriter(serviceDownloadTransferSupport);
    }

    @Bean
    ReactiveDownloadWriter serviceReactiveDownloadWriter(DownloadTransferSupport serviceDownloadTransferSupport) {
        return new ReactiveDownloadWriter(serviceDownloadTransferSupport);
    }
}
