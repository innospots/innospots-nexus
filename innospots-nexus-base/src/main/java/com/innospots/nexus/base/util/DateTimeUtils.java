package com.innospots.nexus.base.util;

import cn.hutool.core.util.StrUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;

/**
 * 日期时间格式化与解析工具类。支持多种预定义日期/时间模式，解析时按顺序尝试模式匹配。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class DateTimeUtils {

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    public static final String DEFAULT_SIMPLE_TIME_PATTERN = "HH:mm";
    public static final String DEFAULT_DATETIME_PATTERN = DEFAULT_DATE_PATTERN + " " + DEFAULT_TIME_PATTERN;
    public static final String DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATETIME_COMPACT_MS_PATTERN = "yyyyMMddHHmmss.SSS";
    public static final String DATE_PATTERN_SLASH = "yyyy/MM/dd";
    public static final String DATE_PATTERN_DOT = "yyyy.MM.dd";
    public static final String DATE_PATTERN_NO_SEPARATOR = "yyyyMMdd";
    public static final String DATETIME_PATTERN_SLASH = "yyyy/MM/dd HH:mm:ss";
    public static final String DATETIME_PATTERN_DOT = "yyyy.MM.dd HH:mm:ss";
    public static final String DATETIME_PATTERN_NO_SEPARATOR = "yyyyMMddHHmmss";
    public static final String DATETIME_PATTERN_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATETIME_PATTERN_ISO_WITH_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static final List<String> SUPPORTED_DATE_PATTERNS = List.of(
            DEFAULT_DATE_PATTERN,
            DATE_PATTERN_SLASH,
            DATE_PATTERN_DOT,
            DATE_PATTERN_NO_SEPARATOR
    );

    public static final List<String> SUPPORTED_DATETIME_PATTERNS = List.of(
            DEFAULT_DATETIME_PATTERN,
            DATETIME_MS_PATTERN,
            DATETIME_PATTERN_SLASH,
            DATETIME_PATTERN_DOT,
            DATETIME_PATTERN_NO_SEPARATOR,
            DATETIME_PATTERN_ISO,
            DATETIME_PATTERN_ISO_WITH_MS,
            DATETIME_COMPACT_MS_PATTERN
    );

    private DateTimeUtils() {
    }

    /**
     * 使用默认模式 {@code yyyy-MM-dd HH:mm:ss} 格式化 {@link LocalDateTime}。
     *
     * @param dateTime 待格式化的日期时间
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATETIME_PATTERN);
    }

    /**
     * 使用指定模式格式化 {@link LocalDateTime}。
     *
     * @param dateTime 待格式化的日期时间
     * @param pattern  日期时间模式
     * @return 格式化后的字符串；输入为 null 时返回 null
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(defaultPattern(pattern, DEFAULT_DATETIME_PATTERN)).format(dateTime);
    }

    /**
     * 使用默认模式 {@code yyyy-MM-dd} 格式化 {@link LocalDate}。
     *
     * @param date 待格式化的日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        return format(date, DEFAULT_DATE_PATTERN);
    }

    /**
     * 使用指定模式格式化 {@link LocalDate}。
     *
     * @param date    待格式化的日期
     * @param pattern 日期模式
     * @return 格式化后的字符串；输入为 null 时返回 null
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(defaultPattern(pattern, DEFAULT_DATE_PATTERN)).format(date);
    }

    /**
     * 使用指定模式格式化遗留 {@link Date}（基于非线程安全的 {@link SimpleDateFormat}，仅适用于一次性调用）。
     *
     * @param date    待格式化的日期
     * @param pattern 日期时间模式
     * @return 格式化后的字符串；输入为 null 时返回 null
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(defaultPattern(pattern, DEFAULT_DATETIME_PATTERN)).format(date);
    }

    /**
     * 解析日期时间字符串：优先尝试 ISO-8601 瞬时格式，再依次遍历所有支持的日期时间模式，
     * 最后回退为仅日期解析（补零至午夜）。
     *
     * @param value 待解析的日期时间字符串
     * @return 解析后的 {@link LocalDateTime}；无法解析时返回 null
     */
    public static LocalDateTime parseLocalDateTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        // 优先尝试 ISO 瞬时格式（如 2026-06-07T10:11:12Z）
        LocalDateTime zonedDateTime = parseInstantDateTime(value.trim());
        if (zonedDateTime != null) {
            return zonedDateTime;
        }
        // 依次尝试各支持的日期时间模式
        for (String pattern : SUPPORTED_DATETIME_PATTERNS) {
            LocalDateTime dateTime = parseLocalDateTime(value, pattern);
            if (dateTime != null) {
                return dateTime;
            }
        }
        // 最后回退：仅按日期解析
        LocalDate date = parseLocalDate(value);
        return date == null ? null : date.atStartOfDay();
    }

    /**
     * 使用指定模式解析日期时间字符串。
     *
     * @param value   待解析的字符串
     * @param pattern 日期时间模式
     * @return 解析后的 {@link LocalDateTime}；解析失败时返回 null
     */
    public static LocalDateTime parseLocalDateTime(String value, String pattern) {
        if (StrUtil.isBlank(value) || StrUtil.isBlank(pattern)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 使用支持的日期模式解析日期字符串。
     *
     * @param value 待解析的日期字符串
     * @return 解析后的 {@link LocalDate}；解析失败时返回 null
     */
    public static LocalDate parseLocalDate(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (String pattern : SUPPORTED_DATE_PATTERNS) {
            try {
                return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                // 当前模式不匹配，继续尝试下一个支持的模式
            }
        }
        return null;
    }

    /**
     * 使用指定模式及遗留 {@link SimpleDateFormat} 解析日期字符串。
     *
     * @param value   待解析的字符串
     * @param pattern 日期模式
     * @return 解析后的 {@link Date}；解析失败时返回 null
     */
    public static Date parseDate(String value, String pattern) {
        if (StrUtil.isBlank(value) || StrUtil.isBlank(pattern)) {
            return null;
        }
        try {
            return new SimpleDateFormat(pattern).parse(value.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将 {@link LocalDateTime} 转换为遗留 {@link Date}（系统默认时区）。
     *
     * @param dateTime 待转换的日期时间
     * @return 转换后的 {@link Date}；输入为 null 时返回 null
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将 {@link LocalDateTime} 转换为 {@link Date}，输入为 null 时默认使用当前时间。
     *
     * @param dateTime 待转换的日期时间
     * @return 转换后的 {@link Date}
     */
    public static Date asDate(LocalDateTime dateTime) {
        return toDate(dateTime == null ? LocalDateTime.now() : dateTime);
    }

    /**
     * 将遗留 {@link Date} 转换为 {@link LocalDateTime}（系统默认时区）。
     *
     * @param date 待转换的日期
     * @return 转换后的 {@link LocalDateTime}；输入为 null 时返回 null
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将纪元毫秒数转换为 {@link LocalDateTime}。
     *
     * @param epochMillis 纪元毫秒时间戳
     * @return 转换后的 {@link LocalDateTime}；输入为 null 时返回 null
     */
    public static LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * {@link #toLocalDateTime(Long)} 的别名。
     *
     * @param epochMillis 纪元毫秒时间戳
     * @return 转换后的 {@link LocalDateTime}
     * @see #toLocalDateTime(Long)
     */
    public static LocalDateTime toDateTime(Long epochMillis) {
        return toLocalDateTime(epochMillis);
    }

    /**
     * 将多种日期/时间表示规范化为 {@link LocalDateTime}。
     * 支持：{@link LocalDateTime}、{@link LocalDate}（补零至午夜）、{@link Date}、
     * {@link Long}（纪元毫秒）、{@link String}。
     *
     * @param value 待规范化的值
     * @return 规范化后的 {@link LocalDateTime}；不支持的类型返回 null
     */
    public static LocalDateTime normalizeDateTime(Object value) {
        return switch (value) {
            case null -> null;
            case LocalDateTime dateTime -> dateTime;
            case LocalDate date -> date.atStartOfDay();
            case Date date -> toLocalDateTime(date);
            case Long epochMillis -> toLocalDateTime(epochMillis);
            case String text -> parseLocalDateTime(text);
            default -> null;
        };
    }

    /**
     * 判断遗留 {@link Date} 是否落在今天。
     *
     * @param date 待判断的日期
     * @return 为今天时返回 {@code true}
     */
    public static boolean isToday(Date date) {
        LocalDateTime dateTime = toLocalDateTime(date);
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 计算两个 {@link Date} 之间相差天数的绝对值。
     *
     * @param day1 第一个日期
     * @param day2 第二个日期
     * @return 相差天数；任一输入为 null 时返回 0
     */
    public static int getDiffDay(Date day1, Date day2) {
        if (day1 == null || day2 == null) {
            return 0;
        }
        long diffSeconds = Math.abs(day1.getTime() - day2.getTime()) / 1000;
        return Math.round(diffSeconds / (3600F * 24F));
    }

    /**
     * 将自 {@code startTime} 至当前的耗时格式化为可读字符串。
     *
     * @param startTime 起始时间戳（毫秒）
     * @return 可读的耗时描述
     */
    public static String consume(long startTime) {
        return consume(System.currentTimeMillis(), startTime);
    }

    /**
     * 将两个时间戳之间的耗时格式化为可读字符串，如 "1 hours, 2 minutes, 3 seconds, 456 ms."。
     *
     * @param endTime   结束时间戳（毫秒）
     * @param startTime 起始时间戳（毫秒）
     * @return 可读的耗时描述
     */
    public static String consume(long endTime, long startTime) {
        long elapsed = Math.max(0, endTime - startTime);
        long elapsedSeconds = elapsed / 1000;
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds - hours * 3600) / 60;
        long seconds = elapsedSeconds - hours * 3600 - minutes * 60;
        long milliseconds = elapsed - elapsedSeconds * 1000;

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append(" hours, ");
        }
        if (minutes > 0 || hours > 0) {
            builder.append(minutes).append(" minutes, ");
        }
        if (seconds > 0 || minutes > 0 || hours > 0) {
            builder.append(seconds).append(" seconds, ");
        }
        return builder.append(milliseconds).append(" ms.").toString();
    }

    /**
     * 将两个 {@link LocalDateTime} 之间的耗时格式化为可读字符串。
     *
     * @param endTime   结束时间
     * @param startTime 起始时间
     * @return 可读的耗时描述；任一输入为 null 时返回 null
     */
    public static String consume(LocalDateTime endTime, LocalDateTime startTime) {
        if (endTime == null || startTime == null) {
            return null;
        }
        return consume(toEpochMillis(endTime), toEpochMillis(startTime));
    }

    /**
     * {@link #consume(LocalDateTime, LocalDateTime)} 的别名。
     *
     * @param endTime   结束时间
     * @param startTime 起始时间
     * @return 可读的耗时描述
     * @see #consume(LocalDateTime, LocalDateTime)
     */
    public static String prettyDuration(LocalDateTime endTime, LocalDateTime startTime) {
        return consume(endTime, startTime);
    }

    private static String defaultPattern(String pattern, String defaultPattern) {
        return StrUtil.isBlank(pattern) ? defaultPattern : pattern;
    }

    private static long toEpochMillis(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private static LocalDateTime parseInstantDateTime(String value) {
        try {
            Instant instant = Instant.parse(value);
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            // Instant 解析失败，继续尝试带偏移量的 ISO 格式（如 2026-06-06T10:11:12+08:00）
        }
        try {
            return ZonedDateTime.parse(value).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
