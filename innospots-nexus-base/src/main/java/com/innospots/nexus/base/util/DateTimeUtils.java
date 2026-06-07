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
 * Date and time formatting and parsing utilities. Supports multiple
 * predefined date/datetime patterns and attempts pattern matching
 * in order when parsing.
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

    /** Formats a LocalDateTime using the default pattern {@code yyyy-MM-dd HH:mm:ss}. */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_DATETIME_PATTERN);
    }

    /** Formats a LocalDateTime with the given pattern. Returns null if input is null. */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(defaultPattern(pattern, DEFAULT_DATETIME_PATTERN)).format(dateTime);
    }

    /** Formats a LocalDate using the default pattern {@code yyyy-MM-dd}. */
    public static String format(LocalDate date) {
        return format(date, DEFAULT_DATE_PATTERN);
    }

    /** Formats a LocalDate with the given pattern. Returns null if input is null. */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(defaultPattern(pattern, DEFAULT_DATE_PATTERN)).format(date);
    }

    /** Formats a legacy Date using the given pattern (thread-unsafe SimpleDateFormat, use for one-off calls). */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(defaultPattern(pattern, DEFAULT_DATETIME_PATTERN)).format(date);
    }

    /**
     * Parses a datetime string using ISO-8601 instant format first, then
     * iterates through all supported datetime patterns. Falls back to
     * date-only parsing (midnight).
     */
    public static LocalDateTime parseLocalDateTime(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        // Try ISO instant format first (e.g. 2026-06-07T10:11:12Z)
        LocalDateTime zonedDateTime = parseInstantDateTime(value.trim());
        if (zonedDateTime != null) {
            return zonedDateTime;
        }
        // Try each supported datetime pattern
        for (String pattern : SUPPORTED_DATETIME_PATTERNS) {
            LocalDateTime dateTime = parseLocalDateTime(value, pattern);
            if (dateTime != null) {
                return dateTime;
            }
        }
        // Last resort: parse as date only
        LocalDate date = parseLocalDate(value);
        return date == null ? null : date.atStartOfDay();
    }

    /** Parses a datetime string with a specific pattern. Returns null on failure. */
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

    /** Parses a date string using supported date patterns. Returns null on failure. */
    public static LocalDate parseLocalDate(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (String pattern : SUPPORTED_DATE_PATTERNS) {
            try {
                return LocalDate.parse(value.trim(), DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException e) {
                // Try the next supported pattern.
            }
        }
        return null;
    }

    /** Parses a date string with a specific pattern using legacy SimpleDateFormat. */
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

    /** Converts a LocalDateTime to a legacy Date (system default timezone). */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Converts a LocalDateTime to Date, defaulting to now if null. */
    public static Date asDate(LocalDateTime dateTime) {
        return toDate(dateTime == null ? LocalDateTime.now() : dateTime);
    }

    /** Converts a legacy Date to LocalDateTime (system default timezone). */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /** Converts epoch millis to LocalDateTime. */
    public static LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /** Alias for {@link #toLocalDateTime(Long)}. */
    public static LocalDateTime toDateTime(Long epochMillis) {
        return toLocalDateTime(epochMillis);
    }

    /**
     * Normalizes various date/time representations into a LocalDateTime.
     * Supports: LocalDateTime, LocalDate (midnight), Date, Long (epoch millis), String.
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

    /** Checks if a legacy Date falls on today's date. */
    public static boolean isToday(Date date) {
        LocalDateTime dateTime = toLocalDateTime(date);
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    /** Returns the absolute difference in days between two Dates. */
    public static int getDiffDay(Date day1, Date day2) {
        if (day1 == null || day2 == null) {
            return 0;
        }
        long diffSeconds = Math.abs(day1.getTime() - day2.getTime()) / 1000;
        return Math.round(diffSeconds / (3600F * 24F));
    }

    /** Formats elapsed time from startTime to now as a human-readable string. */
    public static String consume(long startTime) {
        return consume(System.currentTimeMillis(), startTime);
    }

    /**
     * Formats elapsed time between two timestamps as a human-readable string
     * like "1 hours, 2 minutes, 3 seconds, 456 ms."
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

    /** Formats elapsed time between two LocalDateTime values. */
    public static String consume(LocalDateTime endTime, LocalDateTime startTime) {
        if (endTime == null || startTime == null) {
            return null;
        }
        return consume(toEpochMillis(endTime), toEpochMillis(startTime));
    }

    /** Alias for {@link #consume(LocalDateTime, LocalDateTime)}. */
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
            // Try offset-based ISO values such as 2026-06-06T10:11:12+08:00.
        }
        try {
            return ZonedDateTime.parse(value).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
