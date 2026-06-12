package com.innospots.nexus.core.entity;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

import com.innospots.nexus.base.util.IdGenerator;

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
