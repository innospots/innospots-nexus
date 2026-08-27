package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonsTest {

    @Test
    void writesAndReadsJson() {
        Sample sample = new Sample("nexus", 7);

        String json = Jsons.toJson(sample);
        Sample restored = Jsons.fromJson(json, Sample.class);

        assertThat(json).contains("\"name\":\"nexus\"");
        assertThat(restored).isEqualTo(sample);
    }

    @Test
    void readsJsonAsMap() {
        Map<String, Object> map = Jsons.toMap("{\"name\":\"nexus\",\"count\":3}");

        assertThat(map).containsEntry("name", "nexus");
        assertThat(map).containsEntry("count", 3);
    }

    @Test
    void readsJsonArrayAsList() {
        String json = Jsons.toJson(List.of(new Sample("a", 1), new Sample("b", 2)));

        List<Sample> restored = Jsons.fromJsonList(json, Sample.class);

        assertThat(restored).containsExactly(new Sample("a", 1), new Sample("b", 2));
    }

    @Test
    void readsJsonArrayAsSet() {
        Set<String> restored = Jsons.fromJsonSet("[\"nexus\",\"base\",\"nexus\"]", String.class);

        assertThat(restored).containsExactlyInAnyOrder("nexus", "base");
    }

    @Test
    void writesAndReadsJavaTimeValuesAsIsoText() {
        Instant occurredAt = Instant.parse("2026-08-27T03:00:00Z");
        LocalDateTime localDateTime = LocalDateTime.parse("2026-08-27T11:00:00");
        TimeSample sample = new TimeSample(occurredAt, localDateTime);

        String json = Jsons.toJson(sample);
        TimeSample restored = Jsons.fromJson(json, TimeSample.class);

        assertThat(json).contains("2026-08-27T03:00:00Z");
        assertThat(json).contains("2026-08-27T11:00:00");
        assertThat(json).doesNotContain("epochSecond");
        assertThat(restored).isEqualTo(sample);
    }

    @Test
    void readsJsonWithTypeReference() {
        String json = "{\"alpha\":{\"name\":\"nexus\",\"count\":1}}";

        Map<String, Sample> restored = Jsons.fromJson(json, new TypeReference<>() {
        });

        assertThat(restored.get("alpha")).isEqualTo(new Sample("nexus", 1));
    }

    @Test
    void invalidJsonUsesSerializationStatusCode() {
        assertThatThrownBy(() -> Jsons.fromJson("{", Sample.class))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.SERIALIZATION_FAILED.fullCode());
    }

    record Sample(String name, int count) {
    }

    record TimeSample(Instant occurredAt, LocalDateTime localDateTime) {
    }
}
