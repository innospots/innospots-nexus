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
import com.innospots.nexus.console.dictionary.dao.DictionaryTypeDao;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryItemEntity;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.status.DictionaryStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 租户级字典类型持久化与查询。
 */
@RequiredArgsConstructor
public class DictionaryTypeOperator {

    private final DictionaryTypeDao typeDao;
    private final DictionaryItemDao itemDao;
    private final DictionaryConverter converter;

    public PageResult<DictionaryTypeVo> pageTypes(DictionaryTypePageRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        DictionaryTypePageRequest pageRequest = request == null ? new DictionaryTypePageRequest() : request;
        LambdaQueryWrapper<DictionaryTypeEntity> query = ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryTypeEntity>()
                        .orderByAsc(DictionaryTypeEntity::getSortOrder)
                        .orderByAsc(DictionaryTypeEntity::getTypeName),
                ownership);
        applyFilters(query, pageRequest);
        IPage<DictionaryTypeEntity> selectedPage = typeDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<DictionaryTypeVo> records = selectedPage.getRecords().stream()
                .map(converter::toTypeVo)
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public DictionaryTypeVo getType(String dictionaryTypeId) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        return requireType(dictionaryTypeId, ownership).map(converter::toTypeVo)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.TYPE_NOT_FOUND));
    }

    @Transactional
    public DictionaryTypeVo createType(DictionaryTypeCreateRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        Objects.requireNonNull(request, "request");
        if (existsTypeCode(request.typeCode(), request.securityRealm().name(), ownership)) {
            throw NexusException.build(DictionaryStatusCode.TYPE_CODE_DUPLICATED);
        }
        DictionaryTypeEntity entity = new DictionaryTypeEntity();
        ConsoleOwnershipScope.stamp(entity, ownership);
        entity.setTypeCode(request.typeCode());
        entity.setTypeName(request.typeName());
        entity.setSecurityRealm(request.securityRealm().name());
        entity.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setBuiltIn(false);
        typeDao.insert(entity);
        return converter.toTypeVo(entity);
    }

    @Transactional
    public DictionaryTypeVo updateType(String dictionaryTypeId, DictionaryTypeUpdateRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        Objects.requireNonNull(request, "request");
        DictionaryTypeEntity entity = requireType(dictionaryTypeId, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.TYPE_NOT_FOUND));
        if (request.typeName() != null) {
            entity.setTypeName(request.typeName());
        }
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
        typeDao.updateById(entity);
        return converter.toTypeVo(entity);
    }

    @Transactional
    public void updateTypeStatus(String dictionaryTypeId, DictionaryTypeStatusUpdateRequest request) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        Objects.requireNonNull(request, "request");
        if (request.status() == null) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        DictionaryTypeEntity entity = requireType(dictionaryTypeId, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.TYPE_NOT_FOUND));
        entity.setStatus(converter.formatStatus(request.status()));
        typeDao.updateById(entity);
    }

    @Transactional
    public void deleteType(String dictionaryTypeId) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        DictionaryTypeEntity entity = requireType(dictionaryTypeId, ownership)
                .orElseThrow(() -> NexusException.build(DictionaryStatusCode.TYPE_NOT_FOUND));
        if (Boolean.TRUE.equals(entity.getBuiltIn())) {
            throw NexusException.build(DictionaryStatusCode.TYPE_PROTECTED);
        }
        itemDao.delete(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryItemEntity>()
                        .eq(DictionaryItemEntity::getTypeCode, entity.getTypeCode()),
                ownership));
        typeDao.deleteById(dictionaryTypeId);
    }

    public List<DictionaryTypeOptionVo> listTypeOptions(BasicStatus status) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireTenantScope();
        LambdaQueryWrapper<DictionaryTypeEntity> query = ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryTypeEntity>()
                        .orderByAsc(DictionaryTypeEntity::getSortOrder)
                        .orderByAsc(DictionaryTypeEntity::getTypeName),
                ownership);
        if (status != null) {
            query.eq(DictionaryTypeEntity::getStatus, status.name());
        }
        return typeDao.selectList(query).stream()
                .map(converter::toTypeOption)
                .toList();
    }

    public Optional<DictionaryTypeEntity> findTypeByCode(String typeCode, ConsoleOwnership ownership) {
        if (typeCode == null || typeCode.isBlank()) {
            return Optional.empty();
        }
        DictionaryTypeEntity entity = typeDao.selectOne(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryTypeEntity>()
                        .eq(DictionaryTypeEntity::getTypeCode, typeCode),
                ownership));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertOwnership(entity, ownership);
        return Optional.of(entity);
    }

    private Optional<DictionaryTypeEntity> requireType(String dictionaryTypeId, ConsoleOwnership ownership) {
        if (dictionaryTypeId == null || dictionaryTypeId.isBlank()) {
            return Optional.empty();
        }
        DictionaryTypeEntity entity = typeDao.selectOne(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryTypeEntity>()
                        .eq(DictionaryTypeEntity::getDictionaryTypeId, dictionaryTypeId),
                ownership));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertOwnership(entity, ownership);
        return Optional.of(entity);
    }

    private boolean existsTypeCode(String typeCode, String securityRealm, ConsoleOwnership ownership) {
        if (typeCode == null || typeCode.isBlank()) {
            return false;
        }
        return typeDao.selectCount(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<DictionaryTypeEntity>()
                        .eq(DictionaryTypeEntity::getTypeCode, typeCode)
                        .eq(DictionaryTypeEntity::getSecurityRealm, securityRealm),
                ownership)) > 0;
    }

    private static void applyFilters(LambdaQueryWrapper<DictionaryTypeEntity> query, DictionaryTypePageRequest request) {
        if (request.input() != null && !request.input().isBlank()) {
            String input = request.input().trim();
            query.and(wrapper -> wrapper
                    .like(DictionaryTypeEntity::getTypeName, input)
                    .or()
                    .like(DictionaryTypeEntity::getTypeCode, input));
        }
        if (request.status() != null) {
            query.eq(DictionaryTypeEntity::getStatus, request.status().name());
        }
        if (request.builtIn() != null) {
            query.eq(DictionaryTypeEntity::getBuiltIn, request.builtIn());
        }
    }
}
