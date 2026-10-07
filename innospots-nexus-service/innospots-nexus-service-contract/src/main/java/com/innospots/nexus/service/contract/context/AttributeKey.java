package com.innospots.nexus.service.contract.context;

import com.innospots.nexus.base.util.Checks;

/**
 * {@link ContextAttributes} 的类型化键。名称标识属性；类型拒绝不安全转换。
 *
 * @param <T> 属性值类型
 * @param name 唯一属性名
 * @param type 期望的运行时类型
 * @author Smars
 * @date 2026/09/13
 * @see ContextAttributes
 */
public record AttributeKey<T>(String name, Class<T> type) {

    public AttributeKey {
        Checks.notBlank(name, "name");
        Checks.notNull(type, "type");
    }
}
