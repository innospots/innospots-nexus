package com.innospots.nexus.kernel.menu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.menu.domain.entity.MenuEntity;

import static org.assertj.core.api.Assertions.assertThat;

class MenuDaoContractsTest {

    @Test
    void menuDaoExposesMybatisPlusBaseMapperContract() {
        assertThat(BaseMapper.class).isAssignableFrom(MenuDao.class);
        assertThat(MenuDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + MenuEntity.class.getName() + ">"));
    }
}
