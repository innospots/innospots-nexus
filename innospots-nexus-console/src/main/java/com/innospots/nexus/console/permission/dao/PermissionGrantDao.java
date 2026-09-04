package com.innospots.nexus.console.permission.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;

/** 角色和组织单元授权记录的单表数据访问接口。 */
@Mapper
public interface PermissionGrantDao extends BaseMapper<PermissionGrantEntity> {
}
