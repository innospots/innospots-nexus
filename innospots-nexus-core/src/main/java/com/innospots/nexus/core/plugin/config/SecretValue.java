package com.innospots.nexus.core.plugin.config;

import java.util.Arrays;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 可关闭的内存密文包装器，其文本表示始终为遮罩值。
 *
 * <p>所有公开方法均为线程安全；{@link #use} 与 {@link #copy} 不会暴露内部缓冲区。
 */
public final class SecretValue implements AutoCloseable {

    private static final String MASK = "******";

    private final char[] value;

    private SecretValue(char[] value) {
        this.value = value;
    }

    /**
     * 从明文创建密文副本。
     *
     * @param value 非空白明文
     * @return 新的密文包装器
     * @throws NexusException 值为 {@code null} 或空白时
     */
    public static SecretValue of(String value) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, "secret must not be blank");
        }
        return new SecretValue(value.toCharArray());
    }

    /**
     * 使用字符的临时防御性副本执行操作。
     *
     * @param operation 消费临时字符副本的函数
     * @param <T>       操作结果类型
     * @return 操作结果
     * @throws NexusException {@code operation} 为 {@code null} 时
     */
    public synchronized <T> T use(java.util.function.Function<char[], T> operation) {
        if (operation == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID, "secret operation is required");
        }
        char[] copy = Arrays.copyOf(value, value.length);
        try {
            return operation.apply(copy);
        } finally {
            // 临时副本在 finally 中清零，缩短明文驻留窗口。
            Arrays.fill(copy, '\0');
        }
    }

    /**
     * 返回可独立关闭的副本。
     *
     * @return 与当前实例隔离的新密文包装器
     */
    public synchronized SecretValue copy() {
        return new SecretValue(Arrays.copyOf(value, value.length));
    }

    /**
     * 清零当前保留的字符缓冲区。
     *
     * <p>关闭后不得再调用 {@link #use}；重复关闭保持幂等。
     */
    @Override
    public synchronized void close() {
        Arrays.fill(value, '\0');
    }

    @Override
    public String toString() {
        return MASK;
    }
}
