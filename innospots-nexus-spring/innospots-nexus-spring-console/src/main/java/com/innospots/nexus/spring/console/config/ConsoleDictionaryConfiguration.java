package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.dictionary.converter.DictionaryConverter;
import com.innospots.nexus.console.dictionary.dao.DictionaryItemDao;
import com.innospots.nexus.console.dictionary.dao.DictionaryTypeDao;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;

/**
 * {@code console.dictionary} 域 Spring 装配：字典 operator 与转换器（REST 由 platform 实现）。
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.dictionary.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsoleDictionaryConfiguration {

    @Bean
    DictionaryConverter dictionaryConverter() {
        return DictionaryConverter.INSTANCE;
    }

    @Bean
    DictionaryTypeOperator dictionaryTypeOperator(
            DictionaryTypeDao typeDao,
            DictionaryItemDao itemDao,
            DictionaryConverter dictionaryConverter) {
        return new DictionaryTypeOperator(typeDao, itemDao, dictionaryConverter);
    }
}
