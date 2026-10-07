package com.innospots.nexus.console.menu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

import com.innospots.nexus.console.menu.domain.entity.MenuEntity;

/**
 * 项目菜单的 MyBatis-Plus 持久化 Mapper。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {
}
