package com.innospots.nexus.core.quartz;

import com.innospots.nexus.base.domain.enums.TimePeriod;
import org.junit.jupiter.api.Test;
import org.quartz.CronExpression;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CronConverterTest {

    @Test
    void convertsMinuteIntervalToQuartzCron() {
        String cron = CronConverter.everyMinutes(10);

        assertThat(cron).isEqualTo("0 0/10 * * * ?");
        assertThat(CronExpression.isValidExpression(cron)).isTrue();
    }

    @Test
    void convertsHourIntervalWithMinuteAndSecondToQuartzCron() {
        String cron = CronConverter.everyHours(3, LocalTime.of(0, 15, 30));

        assertThat(cron).isEqualTo("30 15 0/3 * * ?");
        assertThat(CronExpression.isValidExpression(cron)).isTrue();
    }

    @Test
    void convertsDailyWeeklyAndMonthlySchedules() {
        assertThat(CronConverter.daily(2, LocalTime.of(9, 30))).isEqualTo("0 30 9 1/2 * ?");
        assertThat(CronConverter.weekly(List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), LocalTime.of(10, 5)))
                .isEqualTo("0 5 10 ? * MON,FRI");
        assertThat(CronConverter.monthly(List.of(1, 15, 31), LocalTime.of(23, 59, 1)))
                .isEqualTo("1 59 23 1,15,31 * ?");
    }

    @Test
    void convertsPeriodAndTimesToQuartzCron() {
        assertThat(CronConverter.convert(TimePeriod.MINUTE, List.of("5"), null))
                .isEqualTo("0 0/5 * * * ?");
        assertThat(CronConverter.convert(TimePeriod.HOUR, List.of("1", "9", "18"), LocalTime.of(0, 20)))
                .isEqualTo("0 20 1,9,18 * * ?");
        assertThat(CronConverter.convert(TimePeriod.MONTH, List.of("2", "4"), LocalTime.of(12, 23)))
                .isEqualTo("0 23 12 2,4 * ?");
    }

    @Test
    void rejectsInvalidTimeParameters() {
        assertThatThrownBy(() -> CronConverter.everyMinutes(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minutes");
        assertThatThrownBy(() -> CronConverter.weekly(List.of(), LocalTime.NOON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("daysOfWeek");
        assertThatThrownBy(() -> CronConverter.monthly(List.of(0), LocalTime.NOON))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dayOfMonth");
    }
}
