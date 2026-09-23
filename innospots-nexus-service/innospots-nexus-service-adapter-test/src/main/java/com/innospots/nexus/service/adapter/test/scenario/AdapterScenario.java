package com.innospots.nexus.service.adapter.test.scenario;

import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 共享黑盒场景契约。
 *
 * @author Smars
 * @date 2026/09/15
 */
public interface AdapterScenario {

    /**
     * 执行场景。
     *
     * @param target 目标宿主
     */
    void run(AdapterTestTarget target);
}
