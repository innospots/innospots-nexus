package com.innospots.nexus.base.util;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import cn.hutool.core.util.StrUtil;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ID 生成工具，提供 Snowflake 分布式 ID、可配置字符集的随机 ID、
 * 时间戳前缀 ID 以及批量生成能力。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class IdGenerator {

    private static final String NUMERIC_CHARS = "0123456789";
    private static final String ALPHANUMERIC_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ALPHANUMERIC_UPPER_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHANUMERIC_LOWER_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final String HEXADECIMAL_CHARS = "0123456789ABCDEF";
    private static final int MAX_NODE_VALUE = 32;
    private static final int DEFAULT_RANDOM_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static volatile IdGenerator global = of(0, 0);

    private final SnowflakeGenerator snowflakeGenerator;

    private IdGenerator(long datacenterId, long workerId) {
        this.snowflakeGenerator = new SnowflakeGenerator(normalize(workerId), normalize(datacenterId));
    }

    /**
     * 创建指定数据中心与 worker ID 的生成器。
     * 两个值均对 32 取模以兼容 Snowflake 节点范围。
     *
     * @param datacenterId 数据中心 ID
     * @param workerId     worker ID
     * @return 生成器实例
     */
    public static IdGenerator of(long datacenterId, long workerId) {
        return new IdGenerator(datacenterId, workerId);
    }

    /**
     * 根据 IP 地址与端口派生节点 ID 并创建生成器。
     *
     * @param address IP 地址
     * @param port    端口号
     * @return 生成器实例
     */
    public static IdGenerator from(String address, int port) {
        return of(port, addressToLong(address));
    }

    /**
     * 重新配置全局单例生成器。
     *
     * @param datacenterId 数据中心 ID
     * @param workerId     worker ID
     */
    public static void configureGlobal(long datacenterId, long workerId) {
        global = of(datacenterId, workerId);
    }

    /**
     * 根据 IP 地址与端口重新配置全局单例生成器。
     *
     * @param address IP 地址
     * @param port    端口号
     */
    public static void configureGlobal(String address, int port) {
        global = from(address, port);
    }

    /**
     * 从全局生成器获取下一个 Snowflake ID。
     *
     * @return Snowflake ID
     */
    public static long next() {
        return global.nextId();
    }

    /**
     * 从全局生成器获取下一个 Snowflake ID 的字符串形式。
     *
     * @return Snowflake ID 字符串
     */
    public static String nextString() {
        return global.nextIdString();
    }

    /**
     * 生成下一个 Snowflake ID（线程安全）。
     *
     * @return Snowflake ID
     */
    public synchronized long nextId() {
        return snowflakeGenerator.next();
    }

    /**
     * 生成下一个 Snowflake ID 的字符串形式。
     *
     * @return Snowflake ID 字符串
     */
    public String nextIdString() {
        return String.valueOf(nextId());
    }

    /**
     * 生成 8 位随机字母数字 ID，可带前缀。
     *
     * @param prefix 可选前缀
     * @return 随机 ID
     */
    public static String random(String prefix) {
        return random(prefix, Type.ALPHANUMERIC, DEFAULT_RANDOM_LENGTH);
    }

    /**
     * 生成 ULID 字符串，可带前缀。
     *
     * @param prefix 可选前缀
     * @return ULID 字符串
     */
    public static String ulid(String prefix) {
        Ulid ulid = UlidCreator.getUlid();
        return prepend(prefix, ulid.toString());
    }

    /**
     * 生成单调递增 ULID 字符串，可带前缀。
     *
     * @param prefix 可选前缀
     * @return 单调 ULID 字符串
     */
    public static String monotonicUlid(String prefix) {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        return prepend(prefix, ulid.toString());
    }

    /**
     * 按指定字符集类型与长度生成随机 ID。
     *
     * @param prefix 可选前缀
     * @param type   字符集类型
     * @param length 随机部分长度
     * @return 随机 ID
     */
    public static String random(String prefix, Type type, int length) {
        validatePositive(length, "length");
        return prepend(prefix, randomPart(charset(type), length));
    }

    /**
     * 生成时间戳前缀 ID 并附加随机后缀。
     * 格式：{@code [prefix]yyyyMMddHHmmss[SSS][random]}。
     *
     * @param prefix         可选前缀
     * @param type           随机部分字符集类型
     * @param randomLength   随机部分长度
     * @param includeMillis  是否包含毫秒
     * @return 时间戳 ID
     */
    public static String timestamp(String prefix, Type type, int randomLength, boolean includeMillis) {
        validatePositive(randomLength, "randomLength");
        String pattern = includeMillis ? "yyyyMMddHHmmssSSS" : "yyyyMMddHHmmss";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        return prepend(prefix, timestamp + randomPart(charset(type), randomLength));
    }

    /**
     * 使用当前毫秒时间戳加 6 位随机字符生成唯一 ID。
     *
     * @param prefix 可选前缀
     * @param type   随机部分字符集类型
     * @return 唯一 ID
     */
    public static String unique(String prefix, Type type) {
        return prepend(prefix, System.currentTimeMillis() + randomPart(charset(type), 6));
    }

    /**
     * 批量生成随机 ID。
     *
     * @param prefix 可选前缀
     * @param type   字符集类型
     * @param length 每个 ID 的随机部分长度
     * @param count  生成数量
     * @return ID 列表
     */
    public static List<String> batch(String prefix, Type type, int length, int count) {
        validatePositive(count, "count");
        List<String> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(random(prefix, type, length));
        }
        return ids;
    }

    private static String randomPart(String charset, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }
        return result.toString();
    }

    private static String charset(Type type) {
        return switch (type == null ? Type.ALPHANUMERIC : type) {
            case NUMERIC -> NUMERIC_CHARS;
            case ALPHANUMERIC -> ALPHANUMERIC_CHARS;
            case ALPHANUMERIC_UPPER -> ALPHANUMERIC_UPPER_CHARS;
            case ALPHANUMERIC_LOWER -> ALPHANUMERIC_LOWER_CHARS;
            case HEXADECIMAL -> HEXADECIMAL_CHARS;
        };
    }

    private static String prepend(String prefix, String value) {
        return StrUtil.isBlank(prefix) ? value : prefix + value;
    }

    private static void validatePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be greater than 0");
        }
    }

    private static long normalize(long value) {
        return Math.floorMod(value, MAX_NODE_VALUE);
    }

    private static long addressToLong(String address) {
        if (StrUtil.isBlank(address)) {
            return 0;
        }
        if (address.matches("[\\d]{1,3}(\\.[\\d]{1,3}){3}")) {
            String[] parts = address.split("\\.");
            long result = 0;
            for (String part : parts) {
                result = (result << 8) + Long.parseLong(part);
            }
            return result;
        }
        return address.hashCode();
    }

    /**
     * 随机 ID 字符集类型。
     *
     * @author Smars
     * @date 2026/09/13
     */
    public enum Type {
        NUMERIC,
        ALPHANUMERIC,
        ALPHANUMERIC_UPPER,
        ALPHANUMERIC_LOWER,
        HEXADECIMAL
    }
}
