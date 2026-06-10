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
 * ID generation utilities: Snowflake-based distributed IDs, random IDs
 * with configurable character sets, timestamp-prefixed IDs, and batch
 * generation.
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
     * Creates an IdGenerator with explicit datacenter and worker IDs.
     * Both values are normalized modulo 32 for Snowflake compatibility.
     */
    public static IdGenerator of(long datacenterId, long workerId) {
        return new IdGenerator(datacenterId, workerId);
    }

    /**
     * Creates an IdGenerator deriving IDs from an IP address and port.
     */
    public static IdGenerator from(String address, int port) {
        return of(port, addressToLong(address));
    }

    /** Reconfigures the global singleton IdGenerator. */
    public static void configureGlobal(long datacenterId, long workerId) {
        global = of(datacenterId, workerId);
    }

    /** Reconfigures the global singleton from IP address and port. */
    public static void configureGlobal(String address, int port) {
        global = from(address, port);
    }

    /** Returns the next Snowflake ID from the global generator. */
    public static long next() {
        return global.nextId();
    }

    /** Returns the next Snowflake ID as a string from the global generator. */
    public static String nextString() {
        return global.nextIdString();
    }

    /** Generates the next Snowflake ID (thread-safe). */
    public synchronized long nextId() {
        return snowflakeGenerator.next();
    }

    /** Generates the next Snowflake ID as a string. */
    public String nextIdString() {
        return String.valueOf(nextId());
    }

    /** Generates a random alphanumeric ID (8 chars) with an optional prefix. */
    public static String random(String prefix) {
        return random(prefix, Type.ALPHANUMERIC, DEFAULT_RANDOM_LENGTH);
    }

    /** Generates a ULID string with an optional prefix. */
    public static String ulid(String prefix) {
        Ulid ulid = UlidCreator.getUlid();
        return prepend(prefix, ulid.toString());
    }

    /** Generates a monotonic ULID string with an optional prefix. */
    public static String monotonicUlid(String prefix) {
        Ulid ulid = UlidCreator.getMonotonicUlid();
        return prepend(prefix, ulid.toString());
    }

    /** Generates a random ID with the specified character type and length. */
    public static String random(String prefix, Type type, int length) {
        validatePositive(length, "length");
        return prepend(prefix, randomPart(charset(type), length));
    }

    /**
     * Generates a timestamp-prefixed ID with a random suffix.
     * Format: {@code [prefix]yyyyMMddHHmmss[SSS][random]}.
     */
    public static String timestamp(String prefix, Type type, int randomLength, boolean includeMillis) {
        validatePositive(randomLength, "randomLength");
        String pattern = includeMillis ? "yyyyMMddHHmmssSSS" : "yyyyMMddHHmmss";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        return prepend(prefix, timestamp + randomPart(charset(type), randomLength));
    }

    /** Generates a unique ID using current millis + 6 random chars. */
    public static String unique(String prefix, Type type) {
        return prepend(prefix, System.currentTimeMillis() + randomPart(charset(type), 6));
    }

    /** Generates a batch of random IDs. */
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

    public enum Type {
        NUMERIC,
        ALPHANUMERIC,
        ALPHANUMERIC_UPPER,
        ALPHANUMERIC_LOWER,
        HEXADECIMAL
    }
}
