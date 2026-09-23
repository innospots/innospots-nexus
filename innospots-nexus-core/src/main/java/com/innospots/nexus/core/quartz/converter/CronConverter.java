package com.innospots.nexus.core.quartz.converter;

import com.innospots.nexus.core.quartz.enums.TimePeriod;
import org.quartz.CronExpression;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 将常见调度时间参数转换为 Quartz Cron 表达式。
 * <p>
 * Quartz Cron 使用六个必填字段：
 * {@code second minute hour day-of-month month day-of-week}。本转换器显式保持映射关系，
 * 调用方可提供领域级时间参数而无需手工拼接 Cron 字符串。
 * </p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see TimePeriod
 */
public final class CronConverter {

    private CronConverter() {
    }

    /**
     * 将通用周期与周期值转换为 Quartz Cron 表达式。
     * <p>
     * 取值规则：
     * {@link TimePeriod#MINUTE} 期望一个分钟间隔；
     * {@link TimePeriod#HOUR} 接受一个间隔或多个选定小时；
     * {@link TimePeriod#DAY} 接受可选的天间隔；
     * {@link TimePeriod#WEEK} 接受星期值，如 {@code 1}、{@code MON} 或 {@code MONDAY}；
     * {@link TimePeriod#MONTH} 接受 1 至 31 的月中日期。
     * </p>
     *
     * @param timePeriod  周期类型
     * @param periodTimes 周期取值列表
     * @param localTime   本地时刻
     * @return Quartz Cron 表达式
     */
    public static String convert(TimePeriod timePeriod, List<String> periodTimes, LocalTime localTime) {
        if (timePeriod == null) {
            throw new IllegalArgumentException("timePeriod must not be null");
        }
        return switch (timePeriod) {
            case MINUTE -> everyMinutes(firstInt(periodTimes, "minutes"));
            case HOUR -> convertHours(periodTimes, localTime);
            case DAY -> daily(firstIntOrDefault(periodTimes, 1), requiredTime(localTime, "localTime"));
            case WEEK -> weekly(parseDaysOfWeek(periodTimes), requiredTime(localTime, "localTime"));
            case MONTH -> monthly(parseDaysOfMonth(periodTimes), requiredTime(localTime, "localTime"));
        };
    }

    /**
     * 构建每 {@code minutes} 分钟触发一次的 Cron 表达式。
     *
     * @param minutes 分钟间隔
     * @return Cron 表达式
     */
    public static String everyMinutes(int minutes) {
        if (minutes < 1 || minutes > 59) {
            throw new IllegalArgumentException("minutes must be between 1 and 59");
        }
        return validate("0 0/" + minutes + " * * * ?");
    }

    /**
     * 构建每 {@code hours} 小时触发一次的 Cron 表达式。
     * <p>{@code localTime} 为 {@code null} 时，分秒字段默认为午夜。</p>
     *
     * @param hours     小时间隔
     * @param localTime 本地时刻
     * @return Cron 表达式
     */
    public static String everyHours(int hours, LocalTime localTime) {
        if (hours < 1 || hours > 23) {
            throw new IllegalArgumentException("hours must be between 1 and 23");
        }
        LocalTime time = localTime == null ? LocalTime.MIDNIGHT : localTime;
        return validate(time.getSecond() + " " + time.getMinute() + " 0/" + hours + " * * ?");
    }

    /**
     * 构建在每天选定小时触发的 Cron 表达式。
     * <p>分秒字段取自 {@code localTime}；为 {@code null} 时使用午夜。</p>
     *
     * @param hours     小时集合
     * @param localTime 本地时刻
     * @return Cron 表达式
     */
    public static String hourlyAt(Collection<Integer> hours, LocalTime localTime) {
        if (hours == null || hours.isEmpty()) {
            throw new IllegalArgumentException("hours must not be empty");
        }
        LocalTime time = localTime == null ? LocalTime.MIDNIGHT : localTime;
        String hourExpression = hours.stream()
                .map(CronConverter::validateHour)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return validate(time.getSecond() + " " + time.getMinute() + " " + hourExpression + " * * ?");
    }

    /**
     * 构建每 {@code days} 天在 {@code localTime} 触发一次的 Cron 表达式。
     *
     * @param days      天间隔
     * @param localTime 本地时刻
     * @return Cron 表达式
     */
    public static String daily(int days, LocalTime localTime) {
        if (days < 1 || days > 31) {
            throw new IllegalArgumentException("days must be between 1 and 31");
        }
        LocalTime time = requiredTime(localTime, "localTime");
        return validate(time.getSecond() + " " + time.getMinute() + " " + time.getHour() + " 1/" + days + " * ?");
    }

    /**
     * 构建在选定星期几于 {@code localTime} 触发的 Cron 表达式。
     *
     * @param daysOfWeek 星期集合
     * @param localTime  本地时刻
     * @return Cron 表达式
     */
    public static String weekly(Collection<DayOfWeek> daysOfWeek, LocalTime localTime) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            throw new IllegalArgumentException("daysOfWeek must not be empty");
        }
        LocalTime time = requiredTime(localTime, "localTime");
        String weekExpression = daysOfWeek.stream()
                .filter(Objects::nonNull)
                .map(CronConverter::quartzDayOfWeek)
                .collect(Collectors.joining(","));
        if (weekExpression.isBlank()) {
            throw new IllegalArgumentException("daysOfWeek must not be empty");
        }
        return validate(time.getSecond() + " " + time.getMinute() + " " + time.getHour() + " ? * " + weekExpression);
    }

    /**
     * 构建在选定月中日期于 {@code localTime} 触发的 Cron 表达式。
     *
     * @param daysOfMonth 月中日期集合
     * @param localTime   本地时刻
     * @return Cron 表达式
     */
    public static String monthly(Collection<Integer> daysOfMonth, LocalTime localTime) {
        if (daysOfMonth == null || daysOfMonth.isEmpty()) {
            throw new IllegalArgumentException("daysOfMonth must not be empty");
        }
        LocalTime time = requiredTime(localTime, "localTime");
        String dayExpression = daysOfMonth.stream()
                .map(CronConverter::validateDayOfMonth)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return validate(time.getSecond() + " " + time.getMinute() + " " + time.getHour() + " " + dayExpression + " * ?");
    }

    private static String convertHours(List<String> periodTimes, LocalTime localTime) {
        if (periodTimes == null || periodTimes.isEmpty()) {
            return hourlyAt(List.of(0), localTime);
        }
        // 单个小时值视为间隔；多个值表示精确小时
        if (periodTimes.size() == 1) {
            return everyHours(firstInt(periodTimes, "hours"), localTime);
        }
        return hourlyAt(periodTimes.stream().map(value -> parseInt(value, "hours")).toList(), localTime);
    }

    private static List<DayOfWeek> parseDaysOfWeek(List<String> periodTimes) {
        if (periodTimes == null || periodTimes.isEmpty()) {
            throw new IllegalArgumentException("daysOfWeek must not be empty");
        }
        return periodTimes.stream()
                .map(CronConverter::parseDayOfWeek)
                .toList();
    }

    private static List<Integer> parseDaysOfMonth(List<String> periodTimes) {
        if (periodTimes == null || periodTimes.isEmpty()) {
            throw new IllegalArgumentException("daysOfMonth must not be empty");
        }
        return periodTimes.stream()
                .map(value -> parseInt(value, "dayOfMonth"))
                .toList();
    }

    private static int firstInt(List<String> values, String fieldName) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return parseInt(values.getFirst(), fieldName);
    }

    private static int firstIntOrDefault(List<String> values, int defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        return parseInt(values.getFirst(), "days");
    }

    private static int parseInt(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be numeric: " + value, e);
        }
    }

    /**
     * 接受遗留风格的数字星期与可读名称，供外部配置使用。
     *
     * @param value 星期值
     * @return 对应的 {@link DayOfWeek}
     */
    private static DayOfWeek parseDayOfWeek(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("daysOfWeek must not be blank");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "MON", "MONDAY" -> DayOfWeek.MONDAY;
            case "2", "TUE", "TUESDAY" -> DayOfWeek.TUESDAY;
            case "3", "WED", "WEDNESDAY" -> DayOfWeek.WEDNESDAY;
            case "4", "THU", "THURSDAY" -> DayOfWeek.THURSDAY;
            case "5", "FRI", "FRIDAY" -> DayOfWeek.FRIDAY;
            case "6", "SAT", "SATURDAY" -> DayOfWeek.SATURDAY;
            case "7", "SUN", "SUNDAY" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("daysOfWeek is invalid: " + value);
        };
    }

    private static int validateHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("hours must be between 0 and 23");
        }
        return hour;
    }

    private static int validateDayOfMonth(int dayOfMonth) {
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("dayOfMonth must be between 1 and 31");
        }
        return dayOfMonth;
    }

    private static LocalTime requiredTime(LocalTime localTime, String fieldName) {
        if (localTime == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return localTime;
    }

    private static String quartzDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "MON";
            case TUESDAY -> "TUE";
            case WEDNESDAY -> "WED";
            case THURSDAY -> "THU";
            case FRIDAY -> "FRI";
            case SATURDAY -> "SAT";
            case SUNDAY -> "SUN";
        };
    }

    /**
     * 返回前确保生成的表达式被 Quartz 接受。
     *
     * @param cronExpression Cron 表达式
     * @return 校验通过的表达式
     */
    private static String validate(String cronExpression) {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("cronExpression is invalid: " + cronExpression);
        }
        return cronExpression;
    }
}
