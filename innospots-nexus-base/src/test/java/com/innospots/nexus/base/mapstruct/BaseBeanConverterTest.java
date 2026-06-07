package com.innospots.nexus.base.mapstruct;

import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BaseBeanConverterTest {

    private final DemoConverter converter = Mappers.getMapper(DemoConverter.class);

    @Test
    void convertsSingleBeanAndCollectionsThroughMapStruct() {
        DemoModel model = new DemoModel();
        model.setId(7L);
        model.setName("nexus");

        DemoEntity entity = converter.modelToEntity(model);
        List<DemoModel> models = converter.entitiesToModels(List.of(entity));

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getName()).isEqualTo("nexus");
        assertThat(models).singleElement().extracting(DemoModel::getName).isEqualTo("nexus");
        assertThat(converter.modelsToEntities(null)).isEmpty();
    }

    @Test
    void providesJsonBridgeMethodsForBeanConverters() {
        String json = converter.mapToJsonStr(Map.of("region", "apac", "enabled", true));

        assertThat(converter.jsonStrToMap(json))
                .containsEntry("region", "apac")
                .containsEntry("enabled", true);
        assertThat(converter.jsonStrToMapStr("{\"name\":\"nexus\"}")).containsEntry("name", "nexus");
        assertThat(converter.jsonStrToList("[\"a\",\"b\"]")).containsExactly("a", "b");
        assertThat(converter.jsonToIntList("[1,2,3]")).containsExactly(1, 2, 3);
        assertThat(converter.jsonStrToListMap("[{\"k\":\"v\"}]").getFirst()).containsEntry("k", "v");
    }

    @Test
    void providesLocalDateTimeStringBridgeMethods() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 6, 10, 11, 12, 123_000_000);

        String formatted = converter.localDateTimeToStr(dateTime);

        assertThat(formatted).isEqualTo("2026-06-06 10:11:12.123");
        assertThat(converter.strToLocalDateTime(formatted)).isEqualTo(dateTime);
    }

    @Mapper(config = BaseMapperConfig.class)
    interface DemoConverter extends BaseBeanConverter<DemoModel, DemoEntity> {
    }

    public static class DemoModel {
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

    public static class DemoEntity {
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
