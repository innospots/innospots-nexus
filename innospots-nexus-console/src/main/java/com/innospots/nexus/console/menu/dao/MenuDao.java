package com.innospots.nexus.console.menu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.menu.domain.entity.MenuEntity;

/**
 * MyBatis-Plus persistence mapper for project menus.
 */
@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {
}
