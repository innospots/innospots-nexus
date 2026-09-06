# 命名与文件布局

## 文件路径

```text
classpath:ui-pages/{moduleKey}/{pageKey}.yaml
```

| 配置项 | 默认值 |
|--------|--------|
| `basePath` | `ui-pages` |
| `fileSuffix` | `.yaml` |

示例：`ui-pages/menu/menu-main.yaml`

## 命名规则

| 项 | 规则 | 示例 |
|----|------|------|
| `page.id` / `pageKey` / 文件名 | kebab-case，三者一致 | `customer-list.yaml` |
| `page.name` | camelCase（可选） | `customerList` |
| `moduleKey` | `[A-Za-z0-9][A-Za-z0-9._-]*` | `menu`, `role` |
| 控制台主页面 | `{domain}-main` | `menu-main`, `role-main` |

## actions 命名

动词 + 领域，清晰可读：

```text
search, resetSearch, deleteRow, exportList, submitForm
```

## components 命名

名词短语，描述片段职责：

```text
searchForm, customerTable, deleteConfirmDialog
```

## 禁止

- `moduleKey` / `pageKey` 含 `..` 或非法字符
- 文件名与 `page.id` 不一致

## 模块放置

页面 YAML 放在宿主应用的 classpath：

```text
ui-pages/{moduleKey}/{pageKey}.yaml
```

`moduleKey`、`pageKey` 由宿主应用约定；技能不绑定具体 Maven 模块路径。
