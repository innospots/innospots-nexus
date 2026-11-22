import {
  createDefaultTranslatableFieldRegistry,
  type TranslatableFieldRegistry,
} from "@pactor-app/i18n";

/**
 * Console YAML 使用的可翻译字段。
 *
 * 字段清单与 Pactor UI 组件契约对应；业务页面优先在字段值上声明
 * `$i18n{key:fallback}`，由 Pactor `localizePageDsl` 统一转换。
 */
export function createConsoleTranslatableFieldRegistry(): TranslatableFieldRegistry {
  const registry = createDefaultTranslatableFieldRegistry();
  return registry
    .register("Typography", ["props.text"])
    .register("Field", ["props.label", "props.labelHint", "props.description"])
    .register("Textarea", [
      "props.label",
      "props.placeholder",
      "props.description",
      "props.rules[*].message",
    ])
    .register("Input", ["props.rules[*].message"])
    .register("Select", ["props.rules[*].message"])
    .register("RadioGroup", [
      "props.label",
      "props.options[*].label",
      "props.rules[*].message",
    ])
    .register("Breadcrumb", ["props.items[*].label"])
    .register("Descriptions", ["props.title", "props.items[*].label"])
    .register("Tabs", ["props.items[*].label"])
    .register("Drawer", ["props.title", "props.cancelText", "props.okText"])
    .register("Alert", ["props.message", "props.description"])
    .register("Header", [
      "props.locales[*].label",
      "props.user.items[*].label",
    ])
    .register("FilterSelect", [
      "props.label",
      "props.placeholder",
      "props.options[*].label",
      "props.rules[*].message",
    ])
    .registerAction("overlay.open", ["params.overlay.title"]);
}

/** Console 服务端转换共享的 Pactor 字段注册表。 */
export const consoleTranslatableFields = createConsoleTranslatableFieldRegistry();
