package com.innospots.nexus.base.execution;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 单次运行的执行上下文。包含不可变输入参数（{@code inputs}）与执行器可在运行期间读写的可变工作内存（{@code context}）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ExecutionRecord
 */
public class ExecutionContext {

    private final String executionId;
    private final Map<String, Object> inputs;
    private final Map<String, Object> context;

    protected ExecutionContext(String executionId) {
        this(executionId, new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    protected ExecutionContext(String executionId, Map<String, Object> inputs, Map<String, Object> context) {
        this.executionId = executionId;
        this.inputs = inputs == null ? new LinkedHashMap<>() : new LinkedHashMap<>(inputs);
        this.context = context == null ? new LinkedHashMap<>() : new LinkedHashMap<>(context);
    }

    /**
     * 使用给定执行 ID 及空 inputs/context 映射创建上下文。
     */
    public static ExecutionContext create(String executionId) {
        return new ExecutionContext(executionId);
    }

    public String executionId() {
        return executionId;
    }

    /**
     * 设置输入参数。输入属于不可变的执行边界。
     */
    public ExecutionContext input(String key, Object value) {
        inputs.put(key, value);
        return this;
    }

    /** 按键获取输入参数。 */
    public Object getInput(String key) {
        return inputs.get(key);
    }

    /** 将输入参数作为 String 获取，不存在时返回 null。 */
    public String getInputString(String key) {
        Object value = getInput(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 将输入参数作为 Integer 获取，不存在时返回 null。 */
    public Integer getInputInteger(String key) {
        return toInteger(getInput(key));
    }

    /** 将输入参数作为 Long 获取，不存在时返回 null。 */
    public Long getInputLong(String key) {
        return toLong(getInput(key));
    }

    /** 返回所有输入参数的不可修改视图。 */
    public Map<String, Object> inputs() {
        return Map.copyOf(inputs);
    }

    /**
     * 向可变工作上下文写入值。与 inputs 不同，context 可在执行期间被执行器读写。
     *
     * @param key   上下文键
     * @param value 上下文值
     * @return 当前上下文实例，支持链式调用
     */
    public ExecutionContext put(String key, Object value) {
        context.put(key, value);
        return this;
    }

    /** 按键获取上下文值。 */
    public Object get(String key) {
        return context.get(key);
    }

    /** 将上下文值作为 String 获取，不存在时返回 null。 */
    public String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 将上下文值作为 Integer 获取，不存在时返回 null。 */
    public Integer getInteger(String key) {
        return toInteger(get(key));
    }

    /** 将上下文值作为 Long 获取，不存在时返回 null。 */
    public Long getLong(String key) {
        return toLong(get(key));
    }

    /** 返回可变上下文映射的不可修改视图。 */
    public Map<String, Object> context() {
        return Map.copyOf(context);
    }

    /**
     * 使用类型安全模式匹配将原始值转为 Integer：
     * Integer 原样返回，Number 调用 intValue()，String 解析为整数，其余返回 null。
     */
    private static Integer toInteger(Object value) {
        return switch (value) {
            case null -> null;
            case Integer integer -> integer;
            case Number number -> number.intValue();
            case String text when !text.isBlank() -> Integer.parseInt(text);
            default -> null;
        };
    }

    /**
     * 将原始值转为 Long：Long 原样返回，Number 调用 longValue()，
     * String 解析为长整数，其余返回 null。
     */
    private static Long toLong(Object value) {
        return switch (value) {
            case null -> null;
            case Long longValue -> longValue;
            case Number number -> number.longValue();
            case String text when !text.isBlank() -> Long.parseLong(text);
            default -> null;
        };
    }
}
