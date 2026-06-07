package com.innospots.nexus.base.json;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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

    record Sample(String name, int count) {
    }
}
