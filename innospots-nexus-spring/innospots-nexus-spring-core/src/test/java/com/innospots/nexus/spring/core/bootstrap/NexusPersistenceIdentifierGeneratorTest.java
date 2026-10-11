package com.innospots.nexus.spring.core.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.innospots.nexus.core.persistence.id.DbPrimaryGenerator;
import com.innospots.nexus.core.resource.domain.entity.MetaResourceEntity;

/**
 * 验证 {@link NexusPersistenceConfiguration} 注册 Nexus 主键生成器。
 */
class NexusPersistenceIdentifierGeneratorTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(NexusPersistenceConfiguration.class));

    @Test
    void registersDbPrimaryGenerator() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(IdentifierGenerator.class);
            assertThat(context.getBean(IdentifierGenerator.class)).isInstanceOf(DbPrimaryGenerator.class);
            IdentifierGenerator generator = context.getBean(IdentifierGenerator.class);
            assertThat(generator.nextUUID(new MetaResourceEntity())).startsWith("res");
        });
    }
}
