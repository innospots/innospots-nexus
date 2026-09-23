package com.innospots.nexus.quarkus.service.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

/**
 * Quarkus 构建期入口。
 */
public final class ServiceProcessor {

    /**
     * 注册扩展特性名。
     *
     * @return 特性构建项
     */
    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("nexus-service");
    }
}
