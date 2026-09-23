package com.innospots.nexus.console.dictionary.operator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.dictionary.converter.DictionaryConverter;
import com.innospots.nexus.console.dictionary.dao.DictionaryItemDao;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryItemEntity;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.console.dictionary.status.DictionaryStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 租户级字典项持久化与查询。
 */
@RequiredArgsConstructor
public class DictionaryItemOperator {

    private final DictionaryTypeOperator typeOperator;
    private final DictionaryItemDao itemDao;
    private final DictionaryConverter converter;

    public PageResult<DictionaryItemVo> pageItems(String typeCode, DictionaryItemPageRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        requireParentType(typeCode, ownership);
        DictionaryItemPageRequest pageRequest = request == null ? new DictionaryItemPageRequest() : request;
        LambdaQueryWrapper<DictionaryItemEntity> query = ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryItemEntity>()
                        .eq(DictionaryItemEntity::getTypeCode, typeCode)
                        .orderByAsc(DictionaryItemEntity::getSortOrder)
                        .orderByAsc(DictionaryItemEntity::getItemName),
                ownership);
        if (pageRequest.status() != null) {
            query.eq(DictionaryItemEntity::getStatus, pageRequest.status().name());
        }
        if (pageRequest.input() != null && !pageRequest.input().isBlank()) {
            String input = pageRequest.input().trim();
            query.and(wrapper -> wrapper
                    .like(DictionaryItemEntity::getItemName, input)
                    .or()
                    .like(DictionaryItemEntity::getItemValue, input));
        }
        IPage<DictionaryItemEntity> selectedPage = itemDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<DictionaryItemVo> records = selectedPage.getRecords().stream()
                .map(converter::toItemVo)
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    @Transactional
    public DictionaryItemVo createItem(String typeCode, DictionaryItemCreateRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        DictionaryTypeEntity parent = requireParentType(typeCode, ownership);
        Objects.requireNonNull(request, "request");
        if (existsItemValue(typeCode, request.itemValue(), parent.getSecurityRealm(), ownership)) {
            throw NexusException.build(DictionaryStatusCode.ITEM_VALUE_DUPLICATED);
        }
        DictionaryItemEntity entity = new DictionaryItemEntity();
        ConsoleOwnershipScope.stamp(entity, ownership);
        entity.setTypeCode(typeCode);
        entity.setItemValue(request.itemValue());
        entity.setItemName(request.itemName());
        entity.setSecurityRealm(parent.getSecurityRealm());
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setBuiltIn(false);
        itemDao.insert(entity);
        return converter.toItemVo(entity);
    }

    @Transactional
    public DictionaryItemVo updateItem(
            String typeCode,
            String dictionaryItemId,
            DictionaryItemUpdateRequest request
    ) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        requireParentType(typeCode, ownership);
        Objects.requireNonNull(request, "request");
        DictionaryItemEntity entity = requireItem(typeCode, dictionaryItemId, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.ITEM_NOT_FOUND));
        if (request.itemName() != null) {
            entity.setItemName(request.itemName());
        }
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
        itemDao.updateById(entity);
        return converter.toItemVo(entity);
    }

    @Transactional
    public void updateItemStatus(
            String typeCode,
            String dictionaryItemId,
            DictionaryItemStatusUpdateRequest request
    ) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        requireParentType(typeCode, ownership);
        Objects.requireNonNull(request, "request");
        if (request.status() == null) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        DictionaryItemEntity entity = requireItem(typeCode, dictionaryItemId, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.ITEM_NOT_FOUND));
        entity.setStatus(converter.formatStatus(request.status()));
        itemDao.updateById(entity);
    }

    @Transactional
    public void deleteItem(String typeCode, String dictionaryItemId) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        requireParentType(typeCode, ownership);
        DictionaryItemEntity entity = requireItem(typeCode, dictionaryItemId, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.ITEM_NOT_FOUND));
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw NexusException.build(DictionaryStatusCode.ITEM_PROTECTED);
        }
        itemDao.deleteById(dictionaryItemId);
    }

    private DictionaryTypeEntity requireParentType(String typeCode, ConsoleOwnership ownership) {
        return typeOperator.findTypeByCode(typeCode, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.TYPE_NOT_FOUND));
    }

    private Optional<DictionaryItemEntity> requireItem(
            String typeCode,
            String dictionaryItemId,
            ConsoleOwnership ownership
    ) {
        if (dictionaryItemId == null || dictionaryItemId.isBlank()) {
            return Optional.empty();
        }
        DictionaryItemEntity entity = itemDao.selectOne(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryItemEntity>()
                        .eq(DictionaryItemEntity::getDictionaryItemId, dictionaryItemId)
                        .eq(DictionaryItemEntity::getTypeCode, typeCode),
                ownership));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertOwnership(entity, ownership);
        return Optional.of(entity);
    }

    private boolean existsItemValue(
            String typeCode,
            String itemValue,
            String securityRealm,
            ConsoleOwnership ownership
    ) {
        if (itemValue == null || itemValue.isBlank()) {
            return false;
        }
        return itemDao.selectCount(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryItemEntity>()
                        .eq(DictionaryItemEntity::getTypeCode, typeCode)
                        .eq(DictionaryItemEntity::getItemValue, itemValue)
                        .eq(DictionaryItemEntity::getSecurityRealm, securityRealm),
                ownership)) > 0;
    }
}
