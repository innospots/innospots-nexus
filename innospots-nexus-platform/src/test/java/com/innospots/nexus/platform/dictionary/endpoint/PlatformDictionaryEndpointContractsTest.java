package com.innospots.nexus.platform.dictionary.endpoint;

import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.dictionary.endpoint.DictionaryItemEndpoint;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformDictionaryEndpointContractsTest {

    @Test
    void implementsConsoleDictionaryEndpointsOnPlatformPaths() {
        assertThat(DictionaryTypeEndpoint.class.isAssignableFrom(PlatformDictionaryTypeEndpoint.class)).isTrue();
        assertThat(PlatformDictionaryTypeEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.DICTIONARY_TYPES_PATH);

        assertThat(DictionaryItemEndpoint.class.isAssignableFrom(PlatformDictionaryItemEndpoint.class)).isTrue();
        assertThat(PlatformDictionaryItemEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo(PlatformConstant.DICTIONARY_ITEMS_PATH);
    }
}
