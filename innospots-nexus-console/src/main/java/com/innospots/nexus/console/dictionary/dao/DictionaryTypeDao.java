package com.innospots.nexus.console.dictionary.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;

/**
 * MyBatis-Plus mapper for dictionary type records.
 */
@Mapper
public interface DictionaryTypeDao extends BaseMapper<DictionaryTypeEntity> {
}
