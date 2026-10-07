package com.innospots.nexus.base.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Bean 属性拷贝与转换工具，封装 Hutool {@link BeanUtil}。
 * 支持单对象与批量拷贝、Bean 与 Map 互转，以及可选的驼峰/下划线命名转换。
 *
 * @author Smars
 * @date 2026/09/13
 * @see BeanUtil
 */
public final class BeanUtils {

    private static final CopyOptions IGNORE_NULL_OPTIONS = CopyOptions.create()
            .ignoreNullValue()
            .ignoreError();

    private BeanUtils() {
    }

    /**
     * 将源对象属性拷贝到目标对象，忽略 null 值与拷贝错误。
     *
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtil.copyProperties(source, target, IGNORE_NULL_OPTIONS);
    }

    /**
     * 将源对象拷贝为指定类型的新实例。
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param <T>         目标类型参数
     * @return 新实例，源为 null 时返回 {@code null}
     */
    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }

    /**
     * 批量将集合元素拷贝为指定类型列表。
     *
     * @param sourceCollection 源集合
     * @param targetClass      目标元素类型
     * @param <S>              源元素类型
     * @param <T>              目标元素类型
     * @return 拷贝后的列表，源为空时返回空列表
     */
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

    /**
     * 将 Bean 转换为 Map，不转下划线且忽略 null。
     *
     * @param source Bean 对象
     * @return 属性映射
     */
    public static Map<String, Object> toMap(Object source) {
        return BeanUtil.beanToMap(source, false, true);
    }

    /**
     * 将 Bean 转换为 Map，可配置下划线命名与 null 忽略策略。
     *
     * @param source      Bean 对象
     * @param underscore  是否将键转为下划线命名
     * @param ignoreNull  是否忽略 null 属性
     * @return 属性映射
     */
    public static Map<String, Object> toMap(Object source, boolean underscore, boolean ignoreNull) {
        return BeanUtil.beanToMap(source, underscore, ignoreNull);
    }

    /**
     * 将 Map 转换为 Bean，默认不自动转驼峰。
     *
     * @param source      属性映射
     * @param targetClass 目标类型
     * @param <T>         目标类型参数
     * @return Bean 实例
     */
    public static <T> T toBean(Map<String, Object> source, Class<T> targetClass) {
        return toBean(source, targetClass, false);
    }

    /**
     * 将 Map 转换为 Bean，可配置下划线键自动转驼峰。
     *
     * @param source      属性映射
     * @param targetClass 目标类型
     * @param underscore  是否自动将下划线键转为驼峰属性
     * @param <T>         目标类型参数
     * @return Bean 实例
     */
    public static <T> T toBean(Map<String, Object> source, Class<T> targetClass, boolean underscore) {
        return BeanUtil.toBean(source, targetClass, CopyOptions.create()
                .ignoreError()
                .setAutoTransCamelCase(underscore));
    }

    /**
     * 批量将 Map 集合转换为 Bean 列表。
     *
     * @param sourceCollection Map 集合
     * @param targetClass      目标类型
     * @param <T>              目标类型参数
     * @return Bean 列表，源为空时返回空列表
     */
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
