package com.innospots.nexus.base.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class IdGeneratorTest {

    @Test
    void generatesIncreasingSnowflakeIds() {
        IdGenerator generator = IdGenerator.of(1, 2);

        long first = generator.nextId();
        long second = generator.nextId();

        assertThat(first).isPositive();
        assertThat(second).isGreaterThan(first);
        assertThat(generator.nextIdString()).isNotBlank();
    }

    @Test
    void buildsGeneratorFromAddressAndPort() {
        IdGenerator generator = IdGenerator.from("127.0.0.1", 8080);

        assertThat(generator.nextId()).isPositive();
    }

    @Test
    void generatesRandomIdsByType() {
        assertThat(IdGenerator.random("N", IdGenerator.Type.NUMERIC, 6)).matches("N[0-9]{6}");
        assertThat(IdGenerator.random("H", IdGenerator.Type.HEXADECIMAL, 8)).matches("H[0-9A-F]{8}");
        assertThat(IdGenerator.random(null, IdGenerator.Type.ALPHANUMERIC_LOWER, 5)).matches("[0-9a-z]{5}");
    }

    @Test
    void generatesTimestampAndBatchIds() {
        String id = IdGenerator.timestamp("T", IdGenerator.Type.ALPHANUMERIC_UPPER, 4, false);
        List<String> ids = IdGenerator.batch("B", IdGenerator.Type.ALPHANUMERIC, 4, 3);

        assertThat(id).matches("T[0-9]{14}[0-9A-Z]{4}");
        assertThat(ids).hasSize(3).allMatch(value -> value.startsWith("B"));
        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void generatesSnowflakeBase62WithOptionalPrefix() {
        IdGenerator generator = IdGenerator.of(3, 4);

        String withPrefix = generator.nextIdBase62("TNT");
        String withoutPrefix = generator.nextIdBase62(null);
        String globalId = IdGenerator.nextBase62("PUS");

        assertThat(withPrefix).startsWith("TNT").matches("TNT[0-9A-Za-z]{11}");
        assertThat(withoutPrefix).matches("[0-9A-Za-z]{11}");
        assertThat(globalId).startsWith("PUS").hasSize(3 + 11);
        assertThat(IdGenerator.snowflakeBase62()).matches("[0-9A-Za-z]{11}");
        assertThat(IdGenerator.encodeBase62(0L)).isEqualTo("00000000000");
        assertThat(IdGenerator.encodeBase62(62L)).isEqualTo("00000000010");
    }

    @Test
    void snowflakeBase62IdsAreUniqueAndMonotonicInSnowflakeSpace() {
        IdGenerator generator = IdGenerator.of(1, 1);
        long first = generator.nextId();
        String firstEncoded = IdGenerator.encodeBase62(first);
        long second = generator.nextId();
        String secondEncoded = IdGenerator.encodeBase62(second);

        assertThat(second).isGreaterThan(first);
        assertThat(firstEncoded).isNotEqualTo(secondEncoded);
        assertThat(generator.nextIdBase62("x"))
                .isNotEqualTo(generator.nextIdBase62("x"));
    }

    @Test
    void generatesUlidIdsWithOptionalPrefix() {
        String id = IdGenerator.ulid("usr");
        String monotonicId = IdGenerator.monotonicUlid("msg");
        String rawId = IdGenerator.ulid(null);

        assertThat(id).matches("usr[0-9A-HJKMNP-TV-Z]{26}");
        assertThat(monotonicId).matches("msg[0-9A-HJKMNP-TV-Z]{26}");
        assertThat(rawId).matches("[0-9A-HJKMNP-TV-Z]{26}");
    }

    @Test
    void rejectsInvalidLengthsAndCounts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> IdGenerator.random("x", IdGenerator.Type.NUMERIC, 0));
        assertThatIllegalArgumentException()
                .isThrownBy(() -> IdGenerator.batch("x", IdGenerator.Type.NUMERIC, 2, 0));
    }
}
