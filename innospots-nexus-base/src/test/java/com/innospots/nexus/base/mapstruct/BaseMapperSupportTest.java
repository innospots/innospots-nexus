package com.innospots.nexus.base.mapstruct;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaseMapperSupportTest {

    @Test
    void mapsListsWithProvidedFunction() {
        List<String> values = BaseMapperSupport.mapList(List.of(1, 2, 3), String::valueOf);

        assertThat(values).containsExactly("1", "2", "3");
    }

    @Test
    void returnsEmptyListForNullInput() {
        assertThat(BaseMapperSupport.mapList(null, String::valueOf)).isEmpty();
    }
}
