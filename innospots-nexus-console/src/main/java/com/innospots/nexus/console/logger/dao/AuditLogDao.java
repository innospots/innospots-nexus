package com.innospots.nexus.console.logger.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;

/**
 * 仅追加审计日志记录的单表 Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface AuditLogDao extends BaseMapper<AuditLogEntity> {
}
