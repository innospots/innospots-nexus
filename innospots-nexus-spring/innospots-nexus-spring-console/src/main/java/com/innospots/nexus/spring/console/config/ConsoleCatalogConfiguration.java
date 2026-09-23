package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.catalog.bootstrap.ConsoleCatalogSyncStartupTask;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.endpoint.ConsoleCatalogEndpoint;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.ClasspathPageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.PageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser.JacksonPageDslParser;

/**
 * {@code console.catalog} 域 Spring 装配。
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.catalog.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleCatalogConfiguration {

    @Bean
    PageDslLoader pageDslLoader() {
        PageDslConfig config = PageDslConfig.defaults();
        return new ClasspathPageDslLoader(config, new JacksonPageDslParser(config), null);
    }

    @Bean
    ConsoleCatalogSyncService consoleCatalogSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            PageDslLoader pageDslLoader) {
        return new ConsoleCatalogSyncService(resourceDao, contributionCatalog, pageDslLoader);
    }

    @Bean
    ConsoleCatalogService consoleCatalogService(ConsoleCatalogResourceDao resourceDao) {
        return new ConsoleCatalogService(resourceDao);
    }

    @Bean
    ConsoleCatalogSyncStartupTask consoleCatalogSyncStartupTask(
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogSyncStartupTask(syncService);
    }

    @Bean
    NexusStartupTask catalogSyncStartupTask(ConsoleCatalogSyncStartupTask task) {
        return task;
    }

    @Bean
    @Lazy
    ConsoleCatalogEndpoint consoleCatalogEndpoint(
            ConsoleCatalogService catalogService,
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogEndpoint(catalogService, syncService);
    }
}
