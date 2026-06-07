package com.innospots.nexus.base.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BeanUtilsTest {

    @Test
    void copiesBeanPropertiesAndIgnoresNullWhenCopyingIntoTarget() {
        Source source = new Source();
        source.setId(9L);
        source.setName(null);
        Target target = new Target();
        target.setName("existing");

        BeanUtils.copyProperties(source, target);

        assertThat(target.getId()).isEqualTo(9L);
        assertThat(target.getName()).isEqualTo("existing");
    }

    @Test
    void convertsBeansCollectionsAndMaps() {
        Source source = new Source();
        source.setId(3L);
        source.setName("nexus");

        Target copied = BeanUtils.copyProperties(source, Target.class);
        List<Target> list = BeanUtils.copyProperties(List.of(source), Target.class);
        Map<String, Object> map = BeanUtils.toMap(source);
        Target fromMap = BeanUtils.toBean(Map.of("id", 5L, "name", "map"), Target.class);

        assertThat(copied.getName()).isEqualTo("nexus");
        assertThat(list).singleElement().extracting(Target::getId).isEqualTo(3L);
        assertThat(map).containsEntry("id", 3L).containsEntry("name", "nexus");
        assertThat(fromMap.getId()).isEqualTo(5L);
        assertThat(fromMap.getName()).isEqualTo("map");
    }

    public static class Source {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Target {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
