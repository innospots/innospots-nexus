package com.innospots.nexus.platform.dictionary.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.operator.DictionaryItemOperator;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.base.thread.TLC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformDictionaryServiceTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void createDictionaryTypeDelegatesWithPlatformOwnership() {
        bindPlatformSession();
        DictionaryTypeOperator typeOperator = mock(DictionaryTypeOperator.class);
        DictionaryItemOperator itemOperator = mock(DictionaryItemOperator.class);
        I18nObject typeName = I18nObject.of("zh-CN", "地区");
        DictionaryTypeCreateRequest request = new DictionaryTypeCreateRequest("region", typeName, null, java.util.List.of());
        DictionaryTypeVo vo = new DictionaryTypeVo(
                "dict-1",
                "region",
                typeName,
                SecurityRealm.PLATFORM,
                BasicStatus.ENABLED,
                10,
                false,
                null,
                null);
        when(typeOperator.createType(eq(request), any(ConsoleOwnership.class))).thenReturn(vo);
        PlatformDictionaryService service = new PlatformDictionaryService(typeOperator, itemOperator);

        DictionaryTypeVo created = service.createDictionaryType(request);

        assertThat(created.securityRealm()).isEqualTo(SecurityRealm.PLATFORM);
        assertThat(created.status()).isEqualTo(BasicStatus.ENABLED);
        verify(typeOperator).createType(eq(request), any(ConsoleOwnership.class));
    }

    private static void bindPlatformSession() {
        ConsoleSessionContext.bindScope(ConsoleSessionScope.PLATFORM);
        TLC.put(TLC.SECURITY_REALM, SecurityRealm.PLATFORM.name());
    }
}
