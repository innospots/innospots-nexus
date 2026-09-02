package com.innospots.nexus.console.plugin.contribution;

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

/** 将 YAML 的通用 map 严格解码为不可变 console@1 声明。 */
public final class ConsolePluginContributionDecoder
        implements PluginContributionDecoder<ConsolePluginContribution> {

    private static final Set<String> CONTRIBUTION_FIELDS = Set.of("type", "majorVersion", "modules");
    private static final Set<String> MODULE_FIELDS = Set.of(
            "moduleKey", "displayName", "description", "pages", "menuTree");
    private static final Set<String> PAGE_FIELDS = Set.of("pageKey", "pagePath", "children");
    private static final Set<String> MENU_FIELDS = Set.of(
            "menuKey", "title", "icon", "orderIndex", "pageKey", "children");

    /** 返回 console@1 类型。 */
    @Override
    public com.innospots.nexus.core.plugin.contribution.PluginContributionType<ConsolePluginContribution> type() {
        return ConsolePluginContribution.TYPE;
    }

    /** 解码模块、页面与菜单树。 */
    @Override
    public ConsolePluginContribution decode(Map<String, Object> declaration) {
        if (declaration == null) {
            invalid("console contribution declaration is required");
        }
        requireFields(declaration, CONTRIBUTION_FIELDS);
        if (!(declaration.get("type") instanceof String type) || !"console".equals(type)) {
            invalid("console contribution type must be console");
        }
        if (integer(declaration.get("majorVersion"), "majorVersion") != 1) {
            invalid("console contribution majorVersion must be 1");
        }
        if (!declaration.containsKey("modules")) {
            invalid("console contribution modules are required");
        }
        return new ConsolePluginContribution(list(declaration.get("modules"), this::module, "modules"));
    }

    private ConsoleModuleDeclaration module(Object value) {
        Map<String, Object> map = object(value, "module");
        requireFields(map, MODULE_FIELDS);
        return new ConsoleModuleDeclaration(
                text(map.get("moduleKey"), "moduleKey"),
                i18n(map.get("displayName"), "displayName"),
                optionalI18n(map.get("description"), "description"),
                requiredList(map, "pages", this::page),
                optionalNonEmptyList(map, "menuTree", this::menu));
    }

    private UiSpecPageDeclaration page(Object value) {
        Map<String, Object> map = object(value, "page");
        requireFields(map, PAGE_FIELDS);
        return new UiSpecPageDeclaration(
                text(map.get("pageKey"), "pageKey"),
                text(map.get("pagePath"), "pagePath"),
                optionalNonEmptyList(map, "children", this::page));
    }

    private MenuDeclaration menu(Object value) {
        Map<String, Object> map = object(value, "menu");
        requireFields(map, MENU_FIELDS);
        String pageKey = optionalText(map.get("pageKey"), "pageKey");
        List<MenuDeclaration> children = optionalNonEmptyList(map, "children", this::menu);
        Object orderValue = map.getOrDefault("orderIndex", 0);
        return new MenuDeclaration(
                text(map.get("menuKey"), "menuKey"),
                i18n(map.get("title"), "title"),
                optionalText(map.get("icon"), "icon"),
                integer(orderValue, "orderIndex"),
                pageKey,
                children);
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
            java.util.function.Function<Object, T> mapper
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

    private static <T> List<T> optionalNonEmptyList(
            Map<String, Object> map,
            String field,
            java.util.function.Function<Object, T> mapper
    ) {
        if (!map.containsKey(field)) {
            return List.of();
        }
        List<T> values = list(map.get(field), mapper, field);
        if (values.isEmpty()) {
            invalid(field + " must not be empty when present");
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
        Map<String, Object> result = new java.util.LinkedHashMap<>();
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
        return value == null ? null : text(value, field);
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
