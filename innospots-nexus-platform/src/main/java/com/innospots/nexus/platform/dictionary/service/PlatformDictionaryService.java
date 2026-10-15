package com.innospots.nexus.platform.dictionary.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
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
import com.innospots.nexus.console.dictionary.operator.DictionaryItemOperator;
import com.innospots.nexus.console.dictionary.operator.DictionaryTypeOperator;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipLevel;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 运营平台字典服务：PLATFORM 会话与归属固化，委托 operator 做基础持久化。
 */
@RequiredArgsConstructor
public class PlatformDictionaryService {

    private final DictionaryTypeOperator typeOperator;
    private final DictionaryItemOperator itemOperator;

    public PageResult<DictionaryTypeVo> pageDictionaryTypes(DictionaryTypePageRequest request) {
        return typeOperator.pageTypes(request, requirePlatformOwnership());
    }

    public DictionaryTypeVo getDictionaryType(String dictionaryTypeId) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        return typeOperator.getType(dictionaryTypeId, requirePlatformOwnership());
    }

    public DictionaryTypeVo createDictionaryType(DictionaryTypeCreateRequest request) {
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        Checks.notBlank(request.typeCode(), "typeCode");
        Checks.notBlank(request.typeName(), "typeName");
        return typeOperator.createType(request, requirePlatformOwnership());
    }

    public DictionaryTypeVo updateDictionaryType(String dictionaryTypeId, DictionaryTypeUpdateRequest request) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        return typeOperator.updateType(dictionaryTypeId, request, requirePlatformOwnership());
    }

    public void updateDictionaryTypeStatus(String dictionaryTypeId, DictionaryTypeStatusUpdateRequest request) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        typeOperator.updateTypeStatus(dictionaryTypeId, request, requirePlatformOwnership());
    }

    public void deleteDictionaryType(String dictionaryTypeId) {
        Checks.notBlank(dictionaryTypeId, "dictionaryTypeId");
        requirePlatformSession();
        typeOperator.deleteType(dictionaryTypeId, requirePlatformOwnership());
    }

    public List<DictionaryTypeOptionVo> listDictionaryTypeOptions(BasicStatus status) {
        requirePlatformSession();
        return typeOperator.listTypeOptions(status, requirePlatformOwnership());
    }

    public PageResult<DictionaryItemVo> pageDictionaryItems(String typeCode, DictionaryItemPageRequest request) {
        Checks.notBlank(typeCode, "typeCode");
        requirePlatformSession();
        return itemOperator.pageItems(typeCode, request, requirePlatformOwnership());
    }

    public DictionaryItemVo createDictionaryItem(String typeCode, DictionaryItemCreateRequest request) {
        Checks.notBlank(typeCode, "typeCode");
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        return itemOperator.createItem(typeCode, request, requirePlatformOwnership());
    }

    public DictionaryItemVo updateDictionaryItem(
            String typeCode,
            String dictionaryItemId,
            DictionaryItemUpdateRequest request
    ) {
        Checks.notBlank(typeCode, "typeCode");
        Checks.notBlank(dictionaryItemId, "dictionaryItemId");
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        return itemOperator.updateItem(typeCode, dictionaryItemId, request, requirePlatformOwnership());
    }

    public void updateDictionaryItemStatus(
            String typeCode,
            String dictionaryItemId,
            DictionaryItemStatusUpdateRequest request
    ) {
        Checks.notBlank(typeCode, "typeCode");
        Checks.notBlank(dictionaryItemId, "dictionaryItemId");
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        itemOperator.updateItemStatus(typeCode, dictionaryItemId, request, requirePlatformOwnership());
    }

    public void deleteDictionaryItem(String typeCode, String dictionaryItemId) {
        Checks.notBlank(typeCode, "typeCode");
        Checks.notBlank(dictionaryItemId, "dictionaryItemId");
        requirePlatformSession();
        itemOperator.deleteItem(typeCode, dictionaryItemId, requirePlatformOwnership());
    }

    private void requirePlatformSession() {
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private ConsoleOwnership requirePlatformOwnership() {
        requirePlatformSession();
        ConsoleOwnership ownership = ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        if (ownership.ownerType() != RoleOwnerType.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        return ownership;
    }
}
