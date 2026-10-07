package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.dictionary.converter.DictionaryConverter;
import com.innospots.nexus.console.dictionary.dao.DictionaryItemDao;
import com.innospots.nexus.console.dictionary.dao.DictionaryTypeDao;
import com.innospots.nexus.console.dictionary.operator.DictionaryItemOperator;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.console.dictionary.service.DictionaryService;

/**
 * {@code console.dictionary} 域 Quarkus CDI 装配。
 */
@ApplicationScoped
public class ConsoleDictionaryBeans {

    @Produces
    @Singleton
    DictionaryConverter dictionaryConverter() {
        return DictionaryConverter.INSTANCE;
    }

    @Produces
    @Singleton
    DictionaryTypeOperator dictionaryTypeOperator(
            DictionaryTypeDao typeDao,
            DictionaryItemDao itemDao,
            DictionaryConverter dictionaryConverter) {
        return new DictionaryTypeOperator(typeDao, itemDao, dictionaryConverter);
    }

    @Produces
    @Singleton
    DictionaryItemOperator dictionaryItemOperator(
            DictionaryTypeOperator dictionaryTypeOperator,
            DictionaryItemDao itemDao,
            DictionaryConverter dictionaryConverter) {
        return new DictionaryItemOperator(dictionaryTypeOperator, itemDao, dictionaryConverter);
    }

    @Produces
    @Singleton
    DictionaryService dictionaryService(
            DictionaryTypeOperator dictionaryTypeOperator,
            DictionaryItemOperator dictionaryItemOperator) {
        return new DictionaryService(dictionaryTypeOperator, dictionaryItemOperator);
    }
}
