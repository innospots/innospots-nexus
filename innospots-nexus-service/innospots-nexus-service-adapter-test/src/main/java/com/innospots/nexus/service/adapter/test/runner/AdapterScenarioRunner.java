package com.innospots.nexus.service.adapter.test.runner;

import java.util.List;

import com.innospots.nexus.service.adapter.test.scenario.AdapterScenario;
import com.innospots.nexus.service.adapter.test.scenario.GovernanceRateLimitScenario;
import com.innospots.nexus.service.adapter.test.scenario.GovernanceTimeoutScenario;
import com.innospots.nexus.service.adapter.test.scenario.HttpCancellationScenario;
import com.innospots.nexus.service.adapter.test.scenario.HttpContextScenario;
import com.innospots.nexus.service.adapter.test.scenario.HttpDownloadScenario;
import com.innospots.nexus.service.adapter.test.scenario.HttpErrorScenario;
import com.innospots.nexus.service.adapter.test.scenario.HttpSecurityScenario;
import com.innospots.nexus.service.adapter.test.scenario.HttpUnaugmentedScenario;
import com.innospots.nexus.service.adapter.test.scenario.ObservabilityTraceScenario;
import com.innospots.nexus.service.adapter.test.scenario.StreamCancelScenario;
import com.innospots.nexus.service.adapter.test.scenario.StreamSseScenario;
import com.innospots.nexus.service.adapter.test.scenario.WebSocketGovernanceRateLimitScenario;
import com.innospots.nexus.service.adapter.test.scenario.WebSocketPermissionScenario;
import com.innospots.nexus.service.adapter.test.scenario.WebSocketSessionScenario;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 顺序执行全部共享场景。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class AdapterScenarioRunner {

    private final List<AdapterScenario> scenarios;

    /**
     * 使用默认场景集创建。
     */
    public AdapterScenarioRunner() {
        this(defaultScenarios());
    }

    /**
     * 使用指定场景集创建。
     *
     * @param scenarios 场景列表
     */
    public AdapterScenarioRunner(List<AdapterScenario> scenarios) {
        this.scenarios = List.copyOf(scenarios);
    }

    /**
     * 返回默认场景列表。
     *
     * @return 场景列表
     */
    public static List<AdapterScenario> defaultScenarios() {
        return List.of(
                new HttpUnaugmentedScenario(),
                new HttpContextScenario(),
                new HttpErrorScenario(),
                new HttpSecurityScenario(),
                new HttpCancellationScenario(),
                new StreamSseScenario(),
                new StreamCancelScenario(),
                new HttpDownloadScenario(),
                new WebSocketSessionScenario(),
                new WebSocketPermissionScenario(),
                new WebSocketGovernanceRateLimitScenario(),
                new GovernanceRateLimitScenario(),
                new GovernanceTimeoutScenario(),
                new ObservabilityTraceScenario());
    }

    /**
     * 依次执行全部场景。
     *
     * @param target 目标宿主
     */
    public void runAll(AdapterTestTarget target) {
        for (AdapterScenario scenario : scenarios) {
            scenario.run(target);
        }
    }
}
