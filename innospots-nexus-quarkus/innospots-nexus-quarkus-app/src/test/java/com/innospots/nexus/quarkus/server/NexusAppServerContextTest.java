package com.innospots.nexus.quarkus.server;

import jakarta.inject.Inject;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;

import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 Quarkus 下 MyBatis-Plus 能把 Core 模块的 {@link PluginInstallationDao}
 * 注册为 CDI Bean，并完成安装表查询。
 */
@QuarkusTest
class NexusAppServerContextTest {

    @Inject
    SqlSessionFactory sqlSessionFactory;

    @Inject
    PluginInstallationDao pluginInstallationDao;

    /**
     * 容器启动后必须存在 SqlSessionFactory，且插件安装 DAO 可执行查询。
     */
    @Test
    void pluginInstallationDaoHasSqlSessionFactory() {
        assertThat(sqlSessionFactory).isNotNull();
        assertThat(pluginInstallationDao.selectCount(null)).isZero();
    }
}
