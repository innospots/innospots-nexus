package com.innospots.nexus.service.adapter.test.runner;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.adapter.test.scenario.AdapterScenario;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 共享黑盒场景注册表契约（不启动宿主、不访问网络）。
 */
class AdapterScenarioRunnerContractsTest {

    @Test
    void defaultScenarioSetIsComplete() {
        assertThat(AdapterScenarioRunner.defaultScenarios()).hasSize(14);
    }

    @Test
    void runnerCopiesScenarioList() {
        AdapterScenarioRunner runner = new AdapterScenarioRunner(AdapterScenarioRunner.defaultScenarios());
        assertThat(runner).isNotNull();
        assertThat(AdapterScenarioRunner.defaultScenarios())
                .allSatisfy(scenario -> assertThat(scenario).isInstanceOf(AdapterScenario.class));
    }
}
