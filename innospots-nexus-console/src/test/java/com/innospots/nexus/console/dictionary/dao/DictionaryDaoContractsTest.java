package com.innospots.nexus.console.dictionary.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.dictionary.domain.entity.DictionaryItemEntity;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryDaoContractsTest {

    @Test
    void dictionaryDaosExposeMybatisPlusBaseMapperContracts() {
        assertThat(BaseMapper.class).isAssignableFrom(DictionaryTypeDao.class);
        assertThat(BaseMapper.class).isAssignableFrom(DictionaryItemDao.class);
        assertThat(DictionaryTypeDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + DictionaryTypeEntity.class.getName() + ">"));
        assertThat(DictionaryItemDao.class.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + DictionaryItemEntity.class.getName() + ">"));
    }
}
