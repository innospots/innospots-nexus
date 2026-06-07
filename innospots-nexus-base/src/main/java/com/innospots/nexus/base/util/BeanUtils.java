package com.innospots.nexus.base.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Bean property copy and conversion utility wrapping Hutool's
 * {@link BeanUtil}. Supports individual and batch copying, bean-to-map
 * and map-to-bean conversion with optional camelCase-to-underscore
 * naming.
 */
public final class BeanUtils {

    private static final CopyOptions IGNORE_NULL_OPTIONS = CopyOptions.create()
            .ignoreNullValue()
            .ignoreError();

    private BeanUtils() {
    }

    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, IGNORE_NULL_OPTIONS);
    }

    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }

    public static <S, T> List<T> copyProperties(Collection<S> sourceCollection, Class<T> targetClass) {
        if (CollUtil.isEmpty(sourceCollection)) {
            return List.of();
        }
        List<T> result = new ArrayList<>(sourceCollection.size());
        for (S source : sourceCollection) {
            result.add(copyProperties(source, targetClass));
        }
        return result;
    }

    public static Map<String, Object> toMap(Object source) {
        return BeanUtil.beanToMap(source, false, true);
    }

    public static Map<String, Object> toMap(Object source, boolean underscore, boolean ignoreNull) {
        return BeanUtil.beanToMap(source, underscore, ignoreNull);
    }

    public static <T> T toBean(Map<String, Object> source, Class<T> targetClass) {
        return toBean(source, targetClass, false);
    }

    public static <T> T toBean(Map<String, Object> source, Class<T> targetClass, boolean underscore) {
        return BeanUtil.toBean(source, targetClass, CopyOptions.create()
                .ignoreError()
                .setAutoTransCamelCase(underscore));
    }

    public static <T> List<T> toBean(Collection<Map<String, Object>> sourceCollection, Class<T> targetClass) {
        if (CollUtil.isEmpty(sourceCollection)) {
            return List.of();
        }
        List<T> result = new ArrayList<>(sourceCollection.size());
        for (Map<String, Object> source : sourceCollection) {
            result.add(toBean(source, targetClass));
        }
        return result;
    }
}
