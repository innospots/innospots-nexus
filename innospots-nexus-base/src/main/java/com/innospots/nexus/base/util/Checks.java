package com.innospots.nexus.base.util;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

import java.util.Collection;

/**
 * 前置条件校验工具，校验失败时抛出携带 {@link NexusStatusCode#INVALID_PARAMETER} 的
 * {@link NexusException}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see NexusException
 * @see NexusStatusCode
 */
public final class Checks {

    private Checks() {
    }

    /**
     * 要求值非 null，否则抛出异常。
     *
     * @param value 待校验值
     * @param name  参数名，用于错误消息
     * @param <T>   值类型
     * @return 原值
     * @throws NexusException 当 {@code value} 为 null 时
     */
    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, name + " must not be null");
        }
        return value;
    }

    /**
     * 要求字符串非空白，否则抛出异常。
     *
     * @param value 待校验文本
     * @param name  参数名，用于错误消息
     * @return 原值
     * @throws NexusException 当 {@code value} 为 null 或空白时
     */
    public static String notBlank(String value, String name) {
        if (StringUtils.isBlank(value)) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, name + " must not be blank");
        }
        return value;
    }

    /**
     * 要求表达式为 true，否则抛出异常。
     *
     * @param expression 必须为 true 的条件
     * @param message    条件为 false 时的错误消息
     * @throws NexusException 当 {@code expression} 为 false 时
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, message);
        }
    }

    /**
     * 要求数值严格为正，否则抛出异常。
     *
     * @param value 待校验数值
     * @param name  参数名，用于错误消息
     * @return 原值
     * @throws NexusException 当 {@code value} 为零或负数时
     */
    public static long positive(long value, String name) {
        if (value <= 0) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, name + " must be positive");
        }
        return value;
    }

    /**
     * 要求集合非 null 且非空，否则抛出异常。
     *
     * @param value 待校验集合
     * @param name  参数名，用于错误消息
     * @param <T>   集合类型
     * @return 原集合
     * @throws NexusException 当 {@code value} 为 null 或空时
     */
    public static <T extends Collection<?>> T notEmpty(T value, String name) {
        if (value == null || value.isEmpty()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, name + " must not be empty");
        }
        return value;
    }
}
