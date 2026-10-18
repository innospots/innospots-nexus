package com.innospots.nexus.spring.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.platform.dictionary.endpoint.PlatformDictionaryTypeEndpoint;
import com.innospots.nexus.platform.dictionary.service.PlatformDictionaryService;

@Configuration
public class PlatformDictionaryConfiguration {

    @Bean
    PlatformDictionaryService platformDictionaryService(DictionaryTypeOperator dictionaryTypeOperator) {
        return new PlatformDictionaryService(dictionaryTypeOperator);
    }

    @Bean
    @Lazy
    PlatformDictionaryTypeEndpoint platformDictionaryTypeEndpoint(PlatformDictionaryService platformDictionaryService) {
        return new PlatformDictionaryTypeEndpoint(platformDictionaryService);
    }
}
