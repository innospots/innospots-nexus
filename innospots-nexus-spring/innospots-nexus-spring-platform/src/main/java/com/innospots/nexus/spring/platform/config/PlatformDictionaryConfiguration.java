package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.dictionary.operator.DictionaryItemOperator;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.platform.dictionary.endpoint.PlatformDictionaryItemEndpoint;
import com.innospots.nexus.platform.dictionary.endpoint.PlatformDictionaryTypeEndpoint;
import com.innospots.nexus.platform.dictionary.service.PlatformDictionaryService;

@Configuration
public class PlatformDictionaryConfiguration {

    @Bean
    PlatformDictionaryService platformDictionaryService(
            DictionaryTypeOperator dictionaryTypeOperator,
            DictionaryItemOperator dictionaryItemOperator) {
        return new PlatformDictionaryService(dictionaryTypeOperator, dictionaryItemOperator);
    }

    @Bean
    @Lazy
    PlatformDictionaryTypeEndpoint platformDictionaryTypeEndpoint(PlatformDictionaryService platformDictionaryService) {
        return new PlatformDictionaryTypeEndpoint(platformDictionaryService);
    }

    @Bean
    @Lazy
    PlatformDictionaryItemEndpoint platformDictionaryItemEndpoint(PlatformDictionaryService platformDictionaryService) {
        return new PlatformDictionaryItemEndpoint(platformDictionaryService);
    }
}
