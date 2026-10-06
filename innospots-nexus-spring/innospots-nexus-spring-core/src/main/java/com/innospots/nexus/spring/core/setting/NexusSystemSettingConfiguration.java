package com.innospots.nexus.spring.core.setting;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.setting.dao.SystemSettingDao;
import com.innospots.nexus.core.setting.operator.SystemSettingOperator;
import com.innospots.nexus.core.setting.service.SystemSettingService;

/**
 * 系统设置持久化与 {@link SystemSettingService} Spring 装配。
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.core.setting.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class NexusSystemSettingConfiguration {

    @Bean
    SystemSettingOperator systemSettingOperator(SystemSettingDao settingDao) {
        return new SystemSettingOperator(settingDao);
    }

    @Bean
    SystemSettingService systemSettingService(SystemSettingOperator systemSettingOperator) {
        return new SystemSettingService(systemSettingOperator);
    }
}
