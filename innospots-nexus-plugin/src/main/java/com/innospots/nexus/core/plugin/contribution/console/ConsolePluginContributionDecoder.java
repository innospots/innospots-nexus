package com.innospots.nexus.core.plugin.contribution.console;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoder;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 将 YAML map 严格解码为不可变 console@2 声明。
 */
public final class ConsolePluginContributionDecoder
        implements PluginContributionDecoder<ConsolePluginContribution> {

    private static final Set<String> ROOT_FIELDS = Set.of(
            "type", "majorVersion", "domainKey", "displayName", "description",
            "orderIndex", "entryPageKey", "modules");
    private static final Set<String> MODULE_FIELDS = Set.of(
            "moduleKey", "displayName", "description", "orderIndex", "entryPageKey",
            "icon", "viewGroups", "operationGroups", "children");
    private static final Set<String> MENU_FIELDS = Set.of(
            "displayName", "description", "orderIndex", "entryPageKey", "icon", "children");
    private static final Set<String> GROUP_FIELDS = Set.of("group", "label");

    @Override
    public com.innospots.nexus.core.plugin.contribution.PluginContributionType<ConsolePluginContribution> type() {
        return ConsolePluginContribution.TYPE;
    }

    @Override
    public ConsolePluginContribution decode(Map<String, Object> declaration) {
        if (declaration == null) {
            invalid("console contribution declaration is required");
        }
        requireFields(declaration, ROOT_FIELDS);
        if (!(declaration.get("type") instanceof String type) || !"console".equals(type)) {
            invalid("console contribution type must be console");
        }
        if (integer(declaration.get("majorVersion"), "majorVersion") != 2) {
            invalid("console contribution majorVersion must be 2");
        }
        if (declaration.containsKey("moduleKey")) {
            invalid("console contribution root must not declare moduleKey");
        }
        if (declaration.containsKey("children")) {
            invalid("console contribution root must use modules[], not children");
        }
        Object entryPageKey = declaration.get("entryPageKey");
        if (entryPageKey != null && !((String) entryPageKey).isBlank()) {
            invalid("console contribution root entryPageKey must be empty");
        }
        return new ConsolePluginContribution(
                text(declaration.get("domainKey"), "domainKey"),
                optionalI18n(declaration.get("displayName"), "displayName"),
                optionalI18n(declaration.get("description"), "description"),
                integer(declaration.getOrDefault("orderIndex", 0), "orderIndex"),
                requiredList(declaration, "modules", this::moduleNode));
    }

    private ConsoleContributeNode moduleNode(Object value) {
        Map<String, Object> map = object(value, "module");
        requireFields(map, MODULE_FIELDS);
        if (!map.containsKey("moduleKey")) {
            invalid("MODULE node requires moduleKey");
        }
        String entry = optionalText(map.get("entryPageKey"), "entryPageKey");
        List<ConsoleContributeNode> children = requiredList(map, "children", this::childNode);
        return new ConsoleContributeNode(
                text(map.get("moduleKey"), "moduleKey"),
                i18n(map.get("displayName"), "displayName"),
                optionalI18n(map.get("description"), "description"),
                integer(map.getOrDefault("orderIndex", 0), "orderIndex"),
                entry,
                optionalText(map.get("icon"), "icon"),
                groupsOrDefault(optionalGroups(map.get("viewGroups")), defaultViewGroups()),
                groupsOrDefault(optionalGroups(map.get("operationGroups")), defaultOperationGroups()),
                children);
    }

    private static List<ConsolePermissionGroupDeclaration> defaultViewGroups() {
        return List.of(new ConsolePermissionGroupDeclaration(
                "default",
                I18nObject.of(Map.of("zh-CN", "页面访问", "en", "Page access"))));
    }

    private static List<ConsolePermissionGroupDeclaration> defaultOperationGroups() {
        return List.of(new ConsolePermissionGroupDeclaration(
                "default",
                I18nObject.of(Map.of("zh-CN", "操作", "en", "Operations"))));
    }

    private static List<ConsolePermissionGroupDeclaration> groupsOrDefault(
            List<ConsolePermissionGroupDeclaration> groups,
            List<ConsolePermissionGroupDeclaration> defaults
    ) {
        if (groups == null || groups.isEmpty()) {
            return defaults;
        }
        return groups;
    }

    private ConsoleContributeNode childNode(Object value) {
        Map<String, Object> map = object(value, "node");
        String entry = optionalText(map.get("entryPageKey"), "entryPageKey");
        if (entry != null) {
            requireFields(map, MENU_FIELDS);
            if (map.containsKey("moduleKey")) {
                invalid("MENU node must not declare moduleKey");
            }
            return new ConsoleContributeNode(
                    null,
                    i18n(map.get("displayName"), "displayName"),
                    optionalI18n(map.get("description"), "description"),
                    integer(map.getOrDefault("orderIndex", 0), "orderIndex"),
                    entry,
                    optionalText(map.get("icon"), "icon"),
                    List.of(),
                    List.of(),
                    List.of());
        }
        requireFields(map, MODULE_FIELDS);
        return moduleNode(value);
    }

    private List<ConsolePermissionGroupDeclaration> optionalGroups(Object value) {
        if (value == null) {
            return List.of();
        }
        return list(value, this::group, "groups");
    }

    private ConsolePermissionGroupDeclaration group(Object value) {
        Map<String, Object> map = object(value, "group");
        requireFields(map, GROUP_FIELDS);
        return new ConsolePermissionGroupDeclaration(
                text(map.get("group"), "group"),
                i18n(map.get("label"), "label"));
    }

    private static I18nObject i18n(Object value, String field) {
        Map<String, Object> map = object(value, field);
        Map<String, String> strings = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!(entry.getValue() instanceof String) || ((String) entry.getValue()).isBlank()) {
                invalid(field + " values must be non-blank strings");
            }
            strings.put(entry.getKey(), (String) entry.getValue());
        }
        return ConsoleI18n.from(strings, field, PluginStatusCode.DSL_STRUCTURE_INVALID);
    }

    private static I18nObject optionalI18n(Object value, String field) {
        return value == null ? I18nObject.of(Map.of()) : i18n(value, field);
    }

    private static <T> List<T> requiredList(
            Map<String, Object> map,
            String field,
            Function<Object, T> mapper
    ) {
        if (!map.containsKey(field)) {
            invalid(field + " is required");
        }
        List<T> values = list(map.get(field), mapper, field);
        if (values.isEmpty()) {
            invalid(field + " must contain at least one item");
        }
        return values;
    }

    private static <T> List<T> list(Object value, Function<Object, T> mapper, String field) {
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?>)) {
            invalid(field + " must be a list");
        }
        List<?> values = (List<?>) value;
        List<T> result = new ArrayList<>();
        for (Object item : values) {
            result.add(mapper.apply(item));
        }
        return List.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value, String field) {
        if (!(value instanceof Map<?, ?>)) {
            invalid(field + " must be an object");
        }
        Map<?, ?> map = (Map<?, ?>) value;
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                invalid(field + " contains a non-text field name");
            }
            result.put((String) entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void requireFields(Map<String, Object> map, Set<String> allowed) {
        if (map == null) {
            invalid("declaration object is required");
        }
        for (String field : map.keySet()) {
            if (!allowed.contains(field)) {
                invalid("unknown console contribution field: " + field);
            }
        }
    }

    private static String text(Object value, String field) {
        if (!(value instanceof String) || ((String) value).isBlank()) {
            invalid(field + " must be a non-blank string");
        }
        return (String) value;
    }

    private static String optionalText(Object value, String field) {
        if (value == null) {
            return null;
        }
        if (value instanceof String string && string.isBlank()) {
            return null;
        }
        return text(value, field);
    }

    private static int integer(Object value, String field) {
        if (!(value instanceof Number)) {
            invalid(field + " must be an integer");
        }
        Number number = (Number) value;
        try {
            BigDecimal decimal = new BigDecimal(number.toString());
            if (decimal.stripTrailingZeros().scale() > 0
                    || decimal.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) < 0
                    || decimal.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) > 0) {
                invalid(field + " must be an integer");
            }
            return decimal.intValue();
        } catch (NumberFormatException exception) {
            throw NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID.fullCode(),
                    field + " must be an integer", exception);
        }
    }

    private static void invalid(String message) {
        throw NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID, message);
    }
}
