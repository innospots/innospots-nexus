package com.innospots.nexus.quarkus.core.setting;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.core.setting.dao.SystemSettingDao;
import com.innospots.nexus.core.setting.operator.SystemSettingOperator;
import com.innospots.nexus.core.setting.service.SystemSettingService;

/**
 * 系统设置 CDI 装配。
 */
@ApplicationScoped
public class NexusSystemSettingBeans {

    @Produces
    @Singleton
    SystemSettingOperator systemSettingOperator(SystemSettingDao settingDao) {
        return new SystemSettingOperator(settingDao);
    }

    @Produces
    @Singleton
    SystemSettingService systemSettingService(SystemSettingOperator systemSettingOperator) {
        return new SystemSettingService(systemSettingOperator);
    }
}
