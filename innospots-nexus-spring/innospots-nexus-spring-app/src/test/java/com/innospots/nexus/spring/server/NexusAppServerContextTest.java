package com.innospots.nexus.spring.server;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 Spring Boot 4 下 MyBatis-Plus 自动配置能创建 {@link SqlSessionFactory}，
 * 从而让插件安装 DAO 完成装配。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NexusAppServerContextTest {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private PluginInstallationDao pluginInstallationDao;

    /**
     * 容器启动后必须存在 SqlSessionFactory，且插件安装 DAO 可执行查询。
     */
    @Test
    void pluginInstallationDaoHasSqlSessionFactory() {
        assertThat(sqlSessionFactory).isNotNull();
        assertThat(pluginInstallationDao.selectCount(null)).isZero();
    }
}
