package com.innospots.nexus.core.quartz;

import com.innospots.nexus.base.domain.enums.TimePeriod;
import org.quartz.CronExpression;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Converts common scheduling time parameters into Quartz cron expressions.
 * <p>
 * Quartz cron uses six required fields:
 * {@code second minute hour day-of-month month day-of-week}. This converter
 * keeps the mapping explicit so callers can provide domain-level time
 * parameters without hand-building cron strings.
 * </p>
 */
public final class CronConverter {

    private CronConverter() {
    }

    /**
     * Converts a generic period plus period values into a Quartz cron expression.
     * <p>
     * Value rules:
     * {@link TimePeriod#MINUTE} expects one interval in minutes;
     * {@link TimePeriod#HOUR} accepts one interval or multiple selected hours;
     * {@link TimePeriod#DAY} accepts an optional day interval;
     * {@link TimePeriod#WEEK} accepts day-of-week values such as {@code 1},
     * {@code MON}, or {@code MONDAY};
     * {@link TimePeriod#MONTH} accepts days of month from 1 to 31.
     * </p>
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
     * Builds a cron expression that fires every {@code minutes} minutes.
     */
    public static String everyMinutes(int minutes) {
        if (minutes < 1 || minutes > 59) {
            throw new IllegalArgumentException("minutes must be between 1 and 59");
        }
        return validate("0 0/" + minutes + " * * * ?");
    }

    /**
     * Builds a cron expression that fires every {@code hours} hours.
     * <p>If {@code localTime} is null, the minute and second fields default to midnight.</p>
     */
    public static String everyHours(int hours, LocalTime localTime) {
        if (hours < 1 || hours > 23) {
            throw new IllegalArgumentException("hours must be between 1 and 23");
        }
        LocalTime time = localTime == null ? LocalTime.MIDNIGHT : localTime;
        return validate(time.getSecond() + " " + time.getMinute() + " 0/" + hours + " * * ?");
    }

    /**
     * Builds a cron expression that fires at selected hours each day.
     * <p>The minute and second fields are taken from {@code localTime}; null uses midnight.</p>
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
     * Builds a cron expression that fires every {@code days} days at {@code localTime}.
     */
    public static String daily(int days, LocalTime localTime) {
        if (days < 1 || days > 31) {
            throw new IllegalArgumentException("days must be between 1 and 31");
        }
        LocalTime time = requiredTime(localTime, "localTime");
        return validate(time.getSecond() + " " + time.getMinute() + " " + time.getHour() + " 1/" + days + " * ?");
    }

    /**
     * Builds a cron expression that fires on selected weekdays at {@code localTime}.
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
     * Builds a cron expression that fires on selected days of month at {@code localTime}.
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
        // A single hour value is treated as an interval; multiple values are exact hours.
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
     * Accepts legacy-style numeric weekdays and readable names for external configuration.
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
     * Ensures every generated expression is accepted by Quartz before returning it.
     */
    private static String validate(String cronExpression) {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("cronExpression is invalid: " + cronExpression);
        }
        return cronExpression;
    }
}
