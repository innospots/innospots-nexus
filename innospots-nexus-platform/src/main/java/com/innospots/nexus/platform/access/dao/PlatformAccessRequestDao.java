package com.innospots.nexus.platform.access.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.innospots.nexus.platform.access.domain.entity.PlatformAccessRequestEntity;

@Mapper
public interface PlatformAccessRequestDao extends BaseMapper<PlatformAccessRequestEntity> {
}
