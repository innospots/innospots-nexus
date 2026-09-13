package com.innospots.nexus.console.catalog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;

/** 宿主级 Console 目录索引 DAO。 */
@Mapper
public interface ConsoleCatalogResourceDao extends BaseMapper<ConsoleCatalogResourceEntity> {
}
