package com.innospots.nexus.base.thread;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 线程本地上下文（Thread-Local Context）—— 类型化的 {@link ThreadLocal} 映射，
 * 用于在异步边界间传播横切状态（追踪 ID、租户 ID、用户 ID、工作区 ID 等）。
 * <p>使用 {@link #scope(Map)} 配合 try-with-resources 进行作用域上下文注入：</p>
 * <pre>{@code
 * try (var scope = TLC.scope(Map.of(TLC.TRACE_ID, "abc"))) {
 *     // 在注入的上下文中运行的代码
 * }
 * }</pre>
 *
 * @author Smars
 * @date 2026/09/13
 * @see SessionContext
 * @see NexusThreadPoolExecutor
 */
public final class TLC {

    public static final String TRACE_ID = "traceId";
    public static final String TENANT_ID = "tenantId";
    public static final String SESSION_ID = "sessionId";
    public static final String CONVERSATION_ID = "conversationId";
    public static final String WORKSPACE_ID = "workspaceId";
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";
    public static final String SECURITY_REALM = "securityRealm";
    public static final String TENANT_MEMBER_ID = "tenantMemberId";
    public static final String PLATFORM_USER_ID = "platformUserId";
    public static final String PROJECT_ID = "projectId";

    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(LinkedHashMap::new);

    private TLC() {
    }

    /**
     * 在上下文中设置租户 ID。传入 null 时移除该条目。
     *
     * @param tenantId 租户 ID
     */
    public static void tenantId(String tenantId) {
        put(TENANT_ID, tenantId);
    }

    /**
     * 从上下文中获取租户 ID。
     *
     * @return 租户 ID，不存在时返回 null
     */
    public static String tenantId() {
        return getString(TENANT_ID);
    }

    /**
     * 在上下文中设置工作区 ID。传入 null 时移除该条目。
     *
     * @param workspaceId 工作区 ID
     */
    public static void workspaceId(String workspaceId) {
        put(WORKSPACE_ID, workspaceId);
    }

    /**
     * 从上下文中获取工作区 ID。
     *
     * @return 工作区 ID，不存在时返回 null
     */
    public static String workspaceId() {
        return getString(WORKSPACE_ID);
    }

    /**
     * 在上下文中设置用户 ID。传入 null 时移除该条目。
     *
     * @param userId 用户 ID
     */
    public static void userId(Long userId) {
        put(USER_ID, userId);
    }

    /**
     * 从上下文中获取用户 ID。
     *
     * @return 用户 ID，不存在时返回 null
     */
    public static Long userId() {
        return getLong(USER_ID);
    }

    /**
     * 在上下文中设置用户名。传入 null 时移除该条目。
     *
     * @param userName 用户名
     */
    public static void userName(String userName) {
        put(USER_NAME, userName);
    }

    /**
     * 从上下文中获取用户名。
     *
     * @return 用户名，不存在时返回 null
     */
    public static String userName() {
        return getString(USER_NAME);
    }

    /**
     * 在上下文中设置安全域。传入 null 时移除该条目。
     *
     * @param securityRealm 安全域
     */
    public static void securityRealm(String securityRealm) {
        put(SECURITY_REALM, securityRealm);
    }

    /**
     * 从上下文中获取安全域。
     *
     * @return 安全域，不存在时返回 null
     */
    public static String securityRealm() {
        return getString(SECURITY_REALM);
    }

    /**
     * 在上下文中设置租户成员 ID。传入 null 时移除该条目。
     *
     * @param tenantMemberId 租户成员 ID
     */
    public static void tenantMemberId(String tenantMemberId) {
        put(TENANT_MEMBER_ID, tenantMemberId);
    }

    /**
     * 从上下文中获取租户成员 ID。
     *
     * @return 租户成员 ID，不存在时返回 null
     */
    public static String tenantMemberId() {
        return getString(TENANT_MEMBER_ID);
    }

    /**
     * 在上下文中设置平台用户 ID。传入 null 时移除该条目。
     *
     * @param platformUserId 平台用户 ID
     */
    public static void platformUserId(String platformUserId) {
        put(PLATFORM_USER_ID, platformUserId);
    }

    /**
     * 从上下文中获取平台用户 ID。
     *
     * @return 平台用户 ID，不存在时返回 null
     */
    public static String platformUserId() {
        return getString(PLATFORM_USER_ID);
    }

    /**
     * 在上下文中设置项目 ID。传入 null 时移除该条目。
     *
     * @param projectId 项目 ID
     */
    public static void projectId(String projectId) {
        put(PROJECT_ID, projectId);
    }

    /**
     * 从上下文中获取项目 ID。
     *
     * @return 项目 ID，不存在时返回 null
     */
    public static String projectId() {
        return getString(PROJECT_ID);
    }

    /**
     * 设置上下文值。null 值会从上下文中移除该键（等效于调用 {@link #remove}）。
     *
     * @param key   上下文键
     * @param value 上下文值
     */
    public static void put(String key, Object value) {
        if (value == null) {
            remove(key);
        } else {
            CONTEXT.get().put(key, value);
        }
    }

    /**
     * 将给定映射中的所有条目写入上下文。映射中的 null 值会移除对应键。
     *
     * @param values 待写入的键值映射
     */
    public static void putAll(Map<String, ?> values) {
        if (values == null) {
            return;
        }
        values.forEach(TLC::put);
    }

    /**
     * 按键获取上下文值。
     *
     * @param key 上下文键
     * @return 上下文值，不存在时返回 null
     */
    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    /**
     * 按键获取上下文值并转为字符串。
     *
     * @param key 上下文键
     * @return 字符串值，不存在时返回 null
     */
    public static String getString(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 按键获取上下文值并转为 {@link Long}，支持类型安全转换：
     * Long 直通、Number → longValue()、String → parseLong()。
     *
     * @param key 上下文键
     * @return Long 值，无法转换时返回 null
     */
    public static Long getLong(String key) {
        Object value = get(key);
        return switch (value) {
            case null -> null;
            case Long longValue -> longValue;
            case Number number -> number.longValue();
            case String text when !text.isBlank() -> Long.parseLong(text);
            default -> null;
        };
    }

    /**
     * 从上下文中移除指定键。
     *
     * @param key 上下文键
     */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    /**
     * 捕获当前上下文的快照（防御性拷贝）。
     *
     * @return 上下文快照
     */
    public static Map<String, Object> snapshot() {
        return new LinkedHashMap<>(CONTEXT.get());
    }

    /**
     * 用给定映射替换整个上下文。
     *
     * @param context 新的上下文映射
     */
    public static void restore(Map<String, ?> context) {
        Map<String, Object> next = new LinkedHashMap<>();
        if (context != null) {
            context.forEach(next::put);
        }
        CONTEXT.set(next);
    }

    /**
     * 创建作用域上下文：将给定值合并到当前上下文，返回在关闭时恢复先前状态的 {@link Scope}。
     * 配合 try-with-resources 使用：
     * <pre>{@code
     * try (var s = TLC.scope(Map.of("txId", "abc"))) { ... }
     * }</pre>
     *
     * @param values 待合并的上下文值
     * @return 可自动关闭的作用域
     */
    public static Scope scope(Map<String, ?> values) {
        Map<String, Object> previous = snapshot();
        Map<String, Object> next = new HashMap<>(previous);
        if (values != null) {
            next.putAll(values);
        }
        restore(next);
        return new Scope(previous);
    }

    /**
     * 返回原始上下文映射（共享引用，非拷贝）。
     *
     * @return 当前线程的上下文映射
     */
    public static Map<String, Object> context() {
        return CONTEXT.get();
    }

    /**
     * 清除当前线程的所有上下文值。
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 可自动关闭的作用域，在 close 时恢复先前的上下文。
     * 由 {@link TLC#scope(Map)} 内部使用。
     *
     * @author Smars
     * @date 2026/09/13
     * @param previous 关闭时需恢复的先前上下文
     */
    public record Scope(Map<String, Object> previous) implements AutoCloseable {

        @Override
        public void close() {
            TLC.restore(previous);
        }
    }
}
