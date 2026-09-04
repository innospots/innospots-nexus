package com.innospots.nexus.spring.console.bootstrap;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.catalog.config.ConsoleCatalogDaoConfiguration;
import com.innospots.nexus.spring.dictionary.config.ConsoleDictionaryDaoConfiguration;
import com.innospots.nexus.spring.logger.config.ConsoleLoggerDaoConfiguration;
import com.innospots.nexus.spring.menu.config.ConsoleMenuDaoConfiguration;
import com.innospots.nexus.spring.permission.config.ConsolePermissionDaoConfiguration;
import com.innospots.nexus.spring.role.config.ConsoleRoleDaoConfiguration;

/**
 * 管理控制台持久化层引导聚合。
 *
 * <p>按 console 业务域拆分 DAO 扫描配置，便于按模块定位 Mapper 注册点。</p>
 */
@Configuration
@Import({
        ConsoleCatalogDaoConfiguration.class,
        ConsolePermissionDaoConfiguration.class,
        ConsoleMenuDaoConfiguration.class,
        ConsoleRoleDaoConfiguration.class,
        ConsoleDictionaryDaoConfiguration.class,
        ConsoleLoggerDaoConfiguration.class
})
public class NexusConsolePersistenceConfiguration {
}
