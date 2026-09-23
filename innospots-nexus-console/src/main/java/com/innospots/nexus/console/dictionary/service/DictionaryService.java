package com.innospots.nexus.console.dictionary.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
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

/**
 * 字典类型与字典项的应用服务。
 */
@RequiredArgsConstructor
public class DictionaryService {

    private final DictionaryTypeOperator typeOperator;
    private final DictionaryItemOperator itemOperator;

    public PageResult<DictionaryTypeVo> pageDictionaryTypes(DictionaryTypePageRequest request) {
        return typeOperator.pageTypes(request);
    }

    public DictionaryTypeVo getDictionaryType(String dictionaryTypeId) {
        return typeOperator.getType(dictionaryTypeId);
    }

    public DictionaryTypeVo createDictionaryType(DictionaryTypeCreateRequest request) {
        return typeOperator.createType(request);
    }

    public DictionaryTypeVo updateDictionaryType(String dictionaryTypeId, DictionaryTypeUpdateRequest request) {
        return typeOperator.updateType(dictionaryTypeId, request);
    }

    public void updateDictionaryTypeStatus(String dictionaryTypeId, DictionaryTypeStatusUpdateRequest request) {
        typeOperator.updateTypeStatus(dictionaryTypeId, request);
    }

    public void deleteDictionaryType(String dictionaryTypeId) {
        typeOperator.deleteType(dictionaryTypeId);
    }

    public List<DictionaryTypeOptionVo> listDictionaryTypeOptions(BasicStatus status) {
        return typeOperator.listTypeOptions(status);
    }

    public PageResult<DictionaryItemVo> pageDictionaryItems(String typeCode, DictionaryItemPageRequest request) {
        return itemOperator.pageItems(typeCode, request);
    }

    public DictionaryItemVo createDictionaryItem(String typeCode, DictionaryItemCreateRequest request) {
        return itemOperator.createItem(typeCode, request);
    }

    public DictionaryItemVo updateDictionaryItem(
            String typeCode,
            String dictionaryItemId,
            DictionaryItemUpdateRequest request
    ) {
        return itemOperator.updateItem(typeCode, dictionaryItemId, request);
    }

    public void updateDictionaryItemStatus(
            String typeCode,
            String dictionaryItemId,
            DictionaryItemStatusUpdateRequest request
    ) {
        itemOperator.updateItemStatus(typeCode, dictionaryItemId, request);
    }

    public void deleteDictionaryItem(String typeCode, String dictionaryItemId) {
        itemOperator.deleteItem(typeCode, dictionaryItemId);
    }
}
