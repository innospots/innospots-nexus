package com.innospots.nexus.spring.core.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import com.innospots.nexus.core.persistence.handler.AuditMetaObjectHandler;

/**
 * 验证 {@link NexusPersistenceConfiguration} 可在最小 Spring 上下文中装配。
 */
class NexusSimpleBootstrapContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void persistenceConfiguration_registersAuditMetaObjectHandler() {
        contextRunner.withUserConfiguration(PersistenceOnlyApplication.class).run(context -> {
            assertThat(context).hasSingleBean(AuditMetaObjectHandler.class);
            assertThat(context).hasSingleBean(DataSource.class);
        });
    }

    @Configuration
    @Import(NexusPersistenceConfiguration.class)
    static class PersistenceOnlyApplication {

        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }
    }
}
