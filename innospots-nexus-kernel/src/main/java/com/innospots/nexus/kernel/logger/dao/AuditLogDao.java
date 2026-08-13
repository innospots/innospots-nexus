package com.innospots.nexus.kernel.logger.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.kernel.logger.domain.entity.AuditLogEntity;

/**
 * Single-table mapper for append-only audit log records.
 */
public interface AuditLogDao extends BaseMapper<AuditLogEntity> {
}
