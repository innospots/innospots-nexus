package com.innospots.nexus.kernel.group.dao;

import java.lang.reflect.Method;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.kernel.group.domain.entity.GroupEntity;
import com.innospots.nexus.kernel.group.domain.entity.GroupMemberEntity;
import com.innospots.nexus.kernel.group.domain.entity.GroupMemberTagEntity;
import com.innospots.nexus.kernel.group.domain.entity.TagEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GroupDaoContractsTest {

    @Test
    void groupDaosBindOneTableEach() {
        assertMapperEntity(GroupDao.class, GroupEntity.class);
        assertMapperEntity(GroupMemberDao.class, GroupMemberEntity.class);
        assertMapperEntity(TagDao.class, TagEntity.class);
        assertMapperEntity(GroupMemberTagDao.class, GroupMemberTagEntity.class);
    }

    @Test
    void groupDaosDoNotDeclareJoinQueries() {
        List<Class<?>> daoTypes = List.of(
                GroupDao.class,
                GroupMemberDao.class,
                TagDao.class,
                GroupMemberTagDao.class);

        assertThat(daoTypes)
                .allSatisfy(daoType -> assertThat(daoType.getDeclaredMethods())
                        .allSatisfy(this::assertNoJoinSelect));
    }

    private void assertNoJoinSelect(Method method) {
        Select select = method.getAnnotation(Select.class);
        if (select != null) {
            assertThat(String.join(" ", select.value()).toUpperCase()).doesNotContain(" JOIN ");
        }
    }

    private static void assertMapperEntity(Class<?> mapperType, Class<?> entityType) {
        assertThat(BaseMapper.class).isAssignableFrom(mapperType);
        assertThat(mapperType.getGenericInterfaces())
                .anySatisfy(genericInterface -> assertThat(genericInterface.getTypeName())
                        .isEqualTo("com.baomidou.mybatisplus.core.mapper.BaseMapper<"
                                + entityType.getName() + ">"));
    }
}
