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
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;
import com.innospots.nexus.console.sitemap.service.SitemapService;
import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;

/**
 * {@code console.catalog} 域 Spring 装配：插件贡献目录索引。
 *
 * @author Smars
 * @date 2026/09/23
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.catalog.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleCatalogConfiguration {

    @Bean
    SitemapConfigLoader sitemapConfigLoader() {
        return new SitemapConfigLoader(
                SitemapYamlConfig.metaSitemapDefaults(),
                new SitemapYamlParser(),
                null);
    }

    @Bean
    SitemapService sitemapService(SitemapConfigLoader sitemapConfigLoader) {
        return new SitemapService(sitemapConfigLoader);
    }

    @Bean
    ConsoleCatalogSyncService consoleCatalogSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog) {
        return new ConsoleCatalogSyncService(resourceDao, contributionCatalog);
    }

    @Bean
    ConsoleCatalogService consoleCatalogService(
            ConsoleCatalogResourceDao resourceDao,
            EndpointGrantManager endpointGrantManager) {
        return new ConsoleCatalogService(resourceDao, endpointGrantManager);
    }

    @Bean
    ConsoleCatalogSyncStartupTask consoleCatalogSyncStartupTask(
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogSyncStartupTask(syncService);
    }

    @Bean
    @Lazy
    ConsoleCatalogEndpoint consoleCatalogEndpoint(
            ConsoleCatalogService catalogService,
            ConsoleCatalogSyncService syncService) {
        return new ConsoleCatalogEndpoint(catalogService, syncService);
    }
}
