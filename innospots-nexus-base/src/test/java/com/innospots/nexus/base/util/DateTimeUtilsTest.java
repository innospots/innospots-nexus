package com.innospots.nexus.base.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DateTimeUtilsTest {

    @Test
    void formatsAndParsesCommonDateTimePatterns() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 6, 10, 11, 12, 123_000_000);

        assertThat(DateTimeUtils.format(dateTime)).isEqualTo("2026-06-06 10:11:12");
        assertThat(DateTimeUtils.format(dateTime, DateTimeUtils.DATETIME_MS_PATTERN))
                .isEqualTo("2026-06-06 10:11:12.123");
        assertThat(DateTimeUtils.parseLocalDateTime("2026-06-06 10:11:12")).isEqualTo(dateTime.withNano(0));
        assertThat(DateTimeUtils.parseLocalDateTime("2026-06-06T10:11:12.123")).isEqualTo(dateTime);
        assertThat(DateTimeUtils.parseLocalDate("20260606")).isEqualTo(LocalDate.of(2026, 6, 6));
    }

    @Test
    void normalizesSupportedTemporalInputs() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 6, 10, 11, 12);
        Date date = DateTimeUtils.toDate(dateTime);

        assertThat(DateTimeUtils.normalizeDateTime(dateTime)).isEqualTo(dateTime);
        assertThat(DateTimeUtils.normalizeDateTime(date)).isEqualTo(dateTime);
        assertThat(DateTimeUtils.normalizeDateTime(date.getTime())).isEqualTo(dateTime);
        assertThat(DateTimeUtils.normalizeDateTime("2026/06/06 10:11:12")).isEqualTo(dateTime);
        assertThat(DateTimeUtils.normalizeDateTime("not-a-date")).isNull();
    }

    @Test
    void describesElapsedTime() {
        long start = 0L;
        long end = 3_725_089L;

        assertThat(DateTimeUtils.consume(end, start)).isEqualTo("1 hours, 2 minutes, 5 seconds, 89 ms.");
    }
}
