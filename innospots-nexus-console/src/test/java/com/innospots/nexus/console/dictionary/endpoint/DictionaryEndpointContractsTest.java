package com.innospots.nexus.console.dictionary.endpoint;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import static org.assertj.core.api.Assertions.assertThat;
class DictionaryEndpointContractsTest {

    @Test
    void dictionaryTypeEndpointOwnsCatalogOperations() throws NoSuchMethodException {
        assertThat(DictionaryTypeEndpoint.class.isInterface()).isFalse();
        assertThat(DictionaryTypeEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/console/dictionary-types");
        assertHttpMethod(DictionaryTypeEndpoint.class, "pageDictionaryTypes",
                GET.class, DictionaryTypePageRequest.class);
        assertHttpMethod(DictionaryTypeEndpoint.class, "getDictionaryType", GET.class, String.class);
        assertHttpMethod(DictionaryTypeEndpoint.class, "createDictionaryType",
                POST.class, DictionaryTypeCreateRequest.class);
        assertHttpMethod(DictionaryTypeEndpoint.class, "updateDictionaryType",
                PUT.class, String.class, DictionaryTypeUpdateRequest.class);
        assertHttpMethod(DictionaryTypeEndpoint.class, "updateDictionaryTypeStatus",
                PUT.class, String.class, DictionaryTypeStatusUpdateRequest.class);
        assertHttpMethod(DictionaryTypeEndpoint.class, "deleteDictionaryType", DELETE.class, String.class);
        assertHttpMethod(DictionaryTypeEndpoint.class, "listDictionaryTypeOptions", GET.class, BasicStatus.class);
    }

    @Test
    void dictionaryItemEndpointIsNestedUnderTypeCode() throws NoSuchMethodException {
        assertThat(DictionaryItemEndpoint.class.isInterface()).isFalse();
        assertThat(DictionaryItemEndpoint.class.getAnnotation(Path.class).value())
                .isEqualTo("/console/dictionary-types/{typeCode}/items");
        assertHttpMethod(DictionaryItemEndpoint.class, "pageDictionaryItems",
                GET.class, String.class, DictionaryItemPageRequest.class);
        assertHttpMethod(DictionaryItemEndpoint.class, "createDictionaryItem",
                POST.class, String.class, DictionaryItemCreateRequest.class);
        assertHttpMethod(DictionaryItemEndpoint.class, "updateDictionaryItem",
                PUT.class, String.class, String.class, DictionaryItemUpdateRequest.class);
        assertHttpMethod(DictionaryItemEndpoint.class, "updateDictionaryItemStatus",
                PUT.class, String.class, String.class, DictionaryItemStatusUpdateRequest.class);
        assertHttpMethod(DictionaryItemEndpoint.class, "deleteDictionaryItem",
                DELETE.class, String.class, String.class);
    }

    @Test
    void requestsAndViewsExposeStableDictionaryData() {
        assertRecordComponents(DictionaryTypeCreateRequest.class,
                "typeCode", "typeName", "securityRealm", "sortOrder");
        assertRecordComponents(DictionaryTypeUpdateRequest.class, "typeName", "sortOrder");
        assertRecordComponents(DictionaryTypeStatusUpdateRequest.class, "status");
        assertThat(DictionaryTypePageRequest.class.isRecord()).isFalse();
        assertRecordComponents(DictionaryItemCreateRequest.class, "itemValue", "itemName", "sortOrder");
        assertRecordComponents(DictionaryItemUpdateRequest.class, "itemName", "sortOrder");
        assertRecordComponents(DictionaryItemStatusUpdateRequest.class, "status");
        assertThat(DictionaryItemPageRequest.class.isRecord()).isFalse();
        assertThat(DictionaryTypeVo.class.isRecord()).isTrue();
        assertThat(DictionaryTypeOptionVo.class.isRecord()).isTrue();
        assertThat(DictionaryItemVo.class.isRecord()).isTrue();
    }

    private static void assertHttpMethod(
            Class<?> endpointType,
            String methodName,
            Class<? extends java.lang.annotation.Annotation> httpAnnotation,
            Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        Method method = endpointType.getMethod(methodName, parameterTypes);
        assertThat(method.getAnnotation(httpAnnotation)).isNotNull();
    }

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(names);
    }
}
