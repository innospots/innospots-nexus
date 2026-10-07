package com.innospots.nexus.core.persistence.id;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

import com.innospots.nexus.base.util.IdGenerator;
import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * Nexus 持久化实体的 MyBatis-Plus 主键生成器。
 *
 * @author Smars
 * @date 2026/09/13
 * @see BaseEntity
 */
public class DbPrimaryGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return IdGenerator.next();
    }

    @Override
    public String nextUUID(Object entity) {
        String prefix = entity instanceof BaseEntity baseEntity
                ? baseEntity.idPrefix()
                : "";
        return IdGenerator.ulid(prefix);
    }
}
