package com.innospots.nexus.console.bootstrap.domain.model;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link BootstrapStage} 顺序契约测试。
 *
 * <p>顺序是启动初始化的核心约束：目录 → 角色 → 角色授权 → 用户 → 用户角色绑定。
 * 任意两步互换都会造成真实故障（例如授权先于目录会因资源键不存在而启动失败），
 * 因此这里把顺序固化为测试。</p>
 */
class BootstrapStageTest {

    @Test
    void stagesFollowDependencyOrder() {
        assertThat(Arrays.stream(BootstrapStage.values()).map(BootstrapStage::code))
                .containsExactly("catalog", "role", "role-grant", "user", "user-role-binding");
    }

    @Test
    void orderValuesIncreaseMonotonically() {
        int previous = Integer.MIN_VALUE;
        for (BootstrapStage stage : BootstrapStage.values()) {
            assertThat(stage.order()).isGreaterThan(previous);
            previous = stage.order();
        }
    }

    @Test
    void catalogRunsBeforeRoleGrant() {
        // 授权键校验依赖目录索引；这是最初导致启动失败的顺序错误。
        assertThat(BootstrapStage.CATALOG.order()).isLessThan(BootstrapStage.ROLE_GRANT.order());
    }

    @Test
    void roleRunsBeforeRoleGrantAndUserBinding() {
        assertThat(BootstrapStage.ROLE.order()).isLessThan(BootstrapStage.ROLE_GRANT.order());
        assertThat(BootstrapStage.ROLE.order())
                .isLessThan(BootstrapStage.USER_ROLE_BINDING.order());
    }

    @Test
    void userRunsBeforeUserRoleBinding() {
        assertThat(BootstrapStage.USER.order())
                .isLessThan(BootstrapStage.USER_ROLE_BINDING.order());
    }

    @Test
    void catalogRunsAfterPluginHost() {
        // 插件宿主（order=100）必须先激活插件，目录才有贡献可同步。
        assertThat(BootstrapStage.CATALOG.order()).isGreaterThan(100);
    }

    @Test
    void taskNameIsNamespaced() {
        assertThat(BootstrapStage.ROLE.taskName()).isEqualTo("console-bootstrap:role");
    }
}
