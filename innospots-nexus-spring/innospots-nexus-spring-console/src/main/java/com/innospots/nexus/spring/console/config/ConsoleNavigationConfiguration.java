package com.innospots.nexus.spring.console.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.navigation.service.MetaSitemapMenuFilter;
import com.innospots.nexus.console.navigation.service.MetaSitemapNavigationConverter;
import com.innospots.nexus.console.navigation.service.NavigationMenuAssembler;
import com.innospots.nexus.console.navigation.service.NavigationMenuComposer;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.sitemap.service.SitemapService;

/**
 * {@code console.navigation} 域 Spring 装配：导航菜单组装（REST 由 platform 实现）。
 */
@Configuration
public class ConsoleNavigationConfiguration {

    @Bean
    NavigationMenuAssembler navigationMenuAssembler(PermissionVisibilityService visibilityService) {
        return new NavigationMenuAssembler(visibilityService);
    }

    @Bean
    MetaSitemapMenuFilter metaSitemapMenuFilter() {
        return new MetaSitemapMenuFilter();
    }

    @Bean
    MetaSitemapNavigationConverter metaSitemapNavigationConverter() {
        return new MetaSitemapNavigationConverter();
    }

    @Bean
    NavigationMenuComposer navigationMenuComposer(
            NavigationMenuAssembler navigationMenuAssembler,
            SitemapService sitemapService,
            MetaSitemapMenuFilter metaSitemapMenuFilter,
            MetaSitemapNavigationConverter metaSitemapNavigationConverter) {
        return new NavigationMenuComposer(
                navigationMenuAssembler,
                sitemapService,
                metaSitemapMenuFilter,
                metaSitemapNavigationConverter);
    }

}
