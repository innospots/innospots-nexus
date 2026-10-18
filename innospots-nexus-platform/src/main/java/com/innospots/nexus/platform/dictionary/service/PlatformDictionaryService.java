package com.innospots.nexus.platform.dictionary.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.operator.DictionaryItemOperator;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;
import com.innospots.nexus.platform.support.PlatformMutationLog;

/**
 * 运营平台字典服务：PLATFORM 会话与归属固化，委托 operator 做基础持久化。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformDictionaryService {

    private final DictionaryTypeOperator typeOperator;
    private final DictionaryItemOperator itemOperator;

    public PageResult<DictionaryTypeVo> pageDictionaryTypes(DictionaryTypePageRequest request) {
        return typeOperator.pageTypes(request, platformOwnership());
    }

    public DictionaryTypeVo getDictionaryType(String dictionaryTypeId) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        return typeOperator.getType(dictionaryTypeId, platformOwnership());
    }

    public DictionaryTypeVo createDictionaryType(DictionaryTypeCreateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.typeCode(), "typeCode");
        Checks.notNull(request.typeName(), "typeName");
        DictionaryTypeVo created = typeOperator.createType(request, platformOwnership());
        PlatformMutationLog.info(
                log,
                "dictionary type created dictionaryTypeId={} typeCode={}",
                created.dictionaryTypeId(),
                created.typeCode());
        return created;
    }

    public DictionaryTypeVo updateDictionaryType(String dictionaryTypeId, DictionaryTypeUpdateRequest request) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        Objects.requireNonNull(request, "request");
        DictionaryTypeVo updated = typeOperator.updateType(dictionaryTypeId, request, platformOwnership());
        PlatformMutationLog.info(
                log,
                "dictionary type updated dictionaryTypeId={} typeCode={}",
                updated.dictionaryTypeId(),
                updated.typeCode());
        return updated;
    }

    public void updateDictionaryTypeStatus(String dictionaryTypeId, DictionaryTypeStatusUpdateRequest request) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        Objects.requireNonNull(request, "request");
        typeOperator.updateTypeStatus(dictionaryTypeId, request, platformOwnership());
        PlatformMutationLog.info(
                log,
                "dictionary type status updated dictionaryTypeId={} status={}",
                dictionaryTypeId,
                request.status());
    }

    public void deleteDictionaryType(String dictionaryTypeId) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        typeOperator.deleteType(dictionaryTypeId, platformOwnership());
        PlatformMutationLog.info(log, "dictionary type deleted dictionaryTypeId={}", dictionaryTypeId);
    }

    public List<DictionaryTypeOptionVo> listDictionaryTypeOptions(BasicStatus status) {
        return typeOperator.listTypeOptions(status, platformOwnership());
    }

    public PageResult<DictionaryItemVo> pageDictionaryItems(String typeCode, DictionaryItemPageRequest request) {
        Checks.notBlank(typeCode, "typeCode");
        return itemOperator.pageItems(typeCode, request, platformOwnership());
    }

    private static ConsoleOwnership platformOwnership() {
        return PlatformConsoleSession.requireOwnership();
    }
}
