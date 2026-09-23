package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.dictionary.converter.DictionaryConverter;
import com.innospots.nexus.console.dictionary.dao.DictionaryItemDao;
import com.innospots.nexus.console.dictionary.dao.DictionaryTypeDao;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryItemEndpoint;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.console.dictionary.operator.DictionaryItemOperator;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.console.dictionary.service.DictionaryService;

/**
 * {@code console.dictionary} 域 Spring 装配。
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

    @Bean
    DictionaryItemOperator dictionaryItemOperator(
            DictionaryTypeOperator dictionaryTypeOperator,
            DictionaryItemDao itemDao,
            DictionaryConverter dictionaryConverter) {
        return new DictionaryItemOperator(dictionaryTypeOperator, itemDao, dictionaryConverter);
    }

    @Bean
    DictionaryService dictionaryService(
            DictionaryTypeOperator dictionaryTypeOperator,
            DictionaryItemOperator dictionaryItemOperator) {
        return new DictionaryService(dictionaryTypeOperator, dictionaryItemOperator);
    }

    @Bean
    @Lazy
    DictionaryTypeEndpoint dictionaryTypeEndpoint(DictionaryService dictionaryService) {
        return new DictionaryTypeEndpoint(dictionaryService);
    }

    @Bean
    @Lazy
    DictionaryItemEndpoint dictionaryItemEndpoint(DictionaryService dictionaryService) {
        return new DictionaryItemEndpoint(dictionaryService);
    }
}
