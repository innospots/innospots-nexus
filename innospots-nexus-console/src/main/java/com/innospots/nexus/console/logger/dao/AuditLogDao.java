package com.innospots.nexus.console.logger.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;

/**
 * Single-table mapper for append-only audit log records.
 */
public interface AuditLogDao extends BaseMapper<AuditLogEntity> {
}
