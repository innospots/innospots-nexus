# MapStruct — Bean Mapping & JSON Conversion

## BaseMapperConfig

**Type:** interface (`@MapperConfig`)

Shared MapStruct mapper configuration used by all domain mappers.

### Annotation Attributes
- `collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY` — uses accessor-only collection mapping
- `nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS` — always performs null checks
- `unmappedTargetPolicy = ReportingPolicy.IGNORE` — silently ignores unmapped target fields

---

## BaseMapperSupport

**Type:** final class

Utility for mapping collections via a mapping function.

### Method
- **Signature:** `mapList(List<S> source, Function<S, T> mapper) → List<T>`
- **Description:** Maps a list using the given function. Returns empty list if source is null/empty.
- **Parameters:** `source` — source list; `mapper` — mapping function
- **Returns:** mapped list

---

## BaseBeanConverter<Model, Entity>

**Type:** interface

Base converter interface between domain models and persistence entities. Provides default methods for list conversion, JSON/map conversion, and date/time formatting.

### Methods

- **Signature:** `entityToModel(Entity entity) → Model`
- **Description:** Converts an entity to a model.
- **Parameters:** `entity` — persistence entity
- **Returns:** domain model

- **Signature:** `modelToEntity(Model model) → Entity`
- **Description:** Converts a model to an entity.
- **Parameters:** `model` — domain model
- **Returns:** persistence entity

- **Signature:** `entitiesToModels(List<Entity> entities) → List<Model>` (default)
- **Description:** Converts a list of entities to models via `BaseMapperSupport.mapList`.

- **Signature:** `modelsToEntities(List<Model> models) → List<Entity>` (default)
- **Description:** Converts a list of models to entities via `BaseMapperSupport.mapList`.

- **Signature:** `jsonStrToMap(String json) → Map<String, Object>` (default)
- **Description:** Parses a JSON string into a map.
- **Returns:** map representation, or empty map on failure

- **Signature:** `mapToJsonStr(Map<String, Object> map) → String` (default)
- **Description:** Serializes a map to a JSON string.

- **Signature:** `jsonStrToMapStr(String json) → Map<String, String>` (default)
- **Description:** Parses a JSON string into a string-valued map.
- **Throws:** NexusException if parsing fails

- **Signature:** `mapStrToJsonStr(Map<String, String> map) → String` (default)
- **Description:** Serializes a string-valued map to a JSON string.

- **Signature:** `jsonStrToList(String json) → List<String>` (default)
- **Description:** Parses a JSON string into a list of strings.

- **Signature:** `listToJsonStr(List<String> list) → String` (default)
- **Description:** Serializes a list of strings to a JSON string.

- **Signature:** `jsonStrToListMap(String json) → List<Map<String, Object>>` (default)
- **Description:** Parses a JSON string into a list of maps.
- **Throws:** NexusException if parsing fails

- **Signature:** `listMapToJsonStr(List<Map<String, Object>> list) → String` (default)
- **Description:** Serializes a list of maps to a JSON string.

- **Signature:** `jsonToIntList(String json) → List<Integer>` (default)
- **Description:** Parses a JSON string into a list of integers.

- **Signature:** `jsonIntToString(List<Integer> list) → String` (default)
- **Description:** Serializes a list of integers to a JSON string.

- **Signature:** `localDateTimeToStr(LocalDateTime localDateTime) → String` (default)
- **Description:** Formats a LocalDateTime using `yyyy-MM-dd HH:mm:ss.SSS`.

- **Signature:** `strToLocalDateTime(String localDateTime) → LocalDateTime` (default)
- **Description:** Parses a string to LocalDateTime using `yyyy-MM-dd HH:mm:ss.SSS`.