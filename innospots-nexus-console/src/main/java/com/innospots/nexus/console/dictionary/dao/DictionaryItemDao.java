package com.innospots.nexus.console.dictionary.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.dictionary.domain.entity.DictionaryItemEntity;

/**
 * MyBatis-Plus mapper for dictionary item records.
 */
@Mapper
public interface DictionaryItemDao extends BaseMapper<DictionaryItemEntity> {
}
