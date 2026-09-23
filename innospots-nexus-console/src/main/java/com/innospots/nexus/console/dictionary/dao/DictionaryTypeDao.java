package com.innospots.nexus.console.dictionary.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;

/**
 * 字典类型记录的 MyBatis-Plus Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface DictionaryTypeDao extends BaseMapper<DictionaryTypeEntity> {
}
