package com.innospots.nexus.base.mapstruct;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.function.Function;

/**
 * Utility for mapping collections via a mapping function.
 */
public final class BaseMapperSupport {

    private BaseMapperSupport() {
    }

    public static <S, T> List<T> mapList(List<S> source, Function<S, T> mapper) {
        if (CollUtil.isEmpty(source)) {
            return List.of();
        }
        return source.stream().map(mapper).toList();
    }
}
