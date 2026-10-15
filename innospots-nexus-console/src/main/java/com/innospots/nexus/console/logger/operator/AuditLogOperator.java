package com.innospots.nexus.console.logger.operator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.logger.converter.AuditLogConverter;
import com.innospots.nexus.console.logger.dao.AuditLogDao;
import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.console.logger.status.AuditLogStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 审计日志只读查询（写入见 {@link InvocationLogOperator}）。
 */
@RequiredArgsConstructor
public class AuditLogOperator {

    private final AuditLogDao auditLogDao;

    public PageResult<AuditLogVo> pageLogs(AuditLogPageRequest request, ConsoleOwnership ownership) {
        Checks.notNull(ownership, "ownership");
        AuditLogPageRequest pageRequest = request == null ? new AuditLogPageRequest() : request;
        LambdaQueryWrapper<AuditLogEntity> query = ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<AuditLogEntity>()
                        .orderByDesc(AuditLogEntity::getOperatedTime),
                ownership);
        applyFilters(query, pageRequest);
        IPage<AuditLogEntity> selectedPage = auditLogDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<AuditLogVo> records = selectedPage.getRecords().stream()
                .map(AuditLogConverter::toVo)
                .toList();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public AuditLogVo getLog(String auditLogId, ConsoleOwnership ownership) {
        Checks.notNull(ownership, "ownership");
        return findLog(auditLogId, ownership)
                .map(AuditLogConverter::toVo)
                .orElseThrow(() -> ConsoleExceptions.build(AuditLogStatusCode.NOT_FOUND));
    }

    private Optional<AuditLogEntity> findLog(String auditLogId, ConsoleOwnership ownership) {
        if (auditLogId == null || auditLogId.isBlank()) {
            return Optional.empty();
        }
        AuditLogEntity entity = auditLogDao.selectOne(ConsoleOwnershipScope.apply(
                new LambdaQueryWrapper<AuditLogEntity>()
                        .eq(AuditLogEntity::getAuditLogId, auditLogId),
                ownership));
        if (entity == null) {
            return Optional.empty();
        }
        ConsoleOwnershipScope.assertOwnership(entity, ownership);
        return Optional.of(entity);
    }

    private static void applyFilters(LambdaQueryWrapper<AuditLogEntity> query, AuditLogPageRequest request) {
        LocalDateTime since = resolveSince(request.timeRange());
        if (since != null) {
            query.ge(AuditLogEntity::getOperatedTime, since);
        }
        if (request.operator() != null && !request.operator().isBlank()) {
            query.like(AuditLogEntity::getActor, request.operator().trim());
        }
        if (request.operationType() != null && !request.operationType().isBlank()) {
            query.eq(AuditLogEntity::getAction, request.operationType().trim());
        }
        if (request.result() != null && !request.result().isBlank()) {
            query.eq(AuditLogEntity::getExecutionResult, request.result().trim());
        }
        if (request.keyword() != null && !request.keyword().isBlank()) {
            String keyword = request.keyword().trim();
            query.and(wrapper -> wrapper
                    .like(AuditLogEntity::getPath, keyword)
                    .or()
                    .like(AuditLogEntity::getAction, keyword)
                    .or()
                    .like(AuditLogEntity::getMessage, keyword));
        }
    }

    private static LocalDateTime resolveSince(String timeRange) {
        if (timeRange == null || timeRange.isBlank()) {
            return LocalDateTime.now().minusDays(7);
        }
        LocalDateTime now = LocalDateTime.now();
        return switch (timeRange.trim()) {
            case "last24h" -> now.minusHours(24);
            case "last30d" -> now.minusDays(30);
            case "last7d" -> now.minusDays(7);
            default -> now.minusDays(7);
        };
    }
}
