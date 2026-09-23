package com.innospots.nexus.base.mapstruct;

import cn.hutool.core.collection.CollUtil;

import java.util.List;
import java.util.function.Function;

/**
 * 通过映射函数转换集合的工具类。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class BaseMapperSupport {

    private BaseMapperSupport() {
    }

    /**
     * 将源列表中的每个元素通过映射函数转换为目标列表。
     *
     * @param source 源列表
     * @param mapper 映射函数
     * @param <S>    源元素类型
     * @param <T>    目标元素类型
     * @return 转换后的列表；源为空时返回空列表
     */
    public static <S, T> List<T> mapList(List<S> source, Function<S, T> mapper) {
        if (CollUtil.isEmpty(source)) {
            return List.of();
        }
        return source.stream().map(mapper).toList();
    }
}
