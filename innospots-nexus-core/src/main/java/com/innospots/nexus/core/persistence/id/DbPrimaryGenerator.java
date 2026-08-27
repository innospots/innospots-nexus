package com.innospots.nexus.core.persistence.id;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

import com.innospots.nexus.base.util.IdGenerator;
import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * MyBatis-Plus primary-key generator for Nexus persistence entities.
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
