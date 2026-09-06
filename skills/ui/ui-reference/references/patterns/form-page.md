# 表单页模式（Form Page）

`page.type: form` · 模板：[page-template-form.yaml](../../ui-page/references/page-template-form.yaml)

## 结构模型

```text
Page
└── Card
    └── Form
        ├── 字段区（Input / Select）
        └── Flex（取消 / 提交）
```

## 骨架

```yaml
body:
  type: Page
  props:
    title: 新建客户
  children:
    - type: Card
      children:
        - type: Form
          props:
            name: main
            layout: vertical          # vertical | horizontal | inline
            initialValues:
              name: ''
          events:
            onSubmit:
              action: call
              params:
                name: submitForm
          children:
            - type: Input
              props:
                name: name
                label: 客户名称
                rules:
                  - required: true
                    message: 请输入客户名称
            - type: Flex
              props:
                gap: middle
                justify: end
              children:
                - type: Button
                  props:
                    text: 取消
                  events:
                    onClick:
                      action: navigate
                      params:
                        to: /customers/list
                - type: Button
                  props:
                    text: 保存
                    variant: primary
                    submit: true
```

## Form 语义（Pactor）

- 字段名用 **`name`**，不用 `field`
- 提交按钮用 **`submit: true`**
- 提交后 `${form.xxx}` / `${state.form.xxx}` 可读表单值
- 不用 `model:` prop 绑定 state

## 布局变体

| `layout` | 场景 |
|----------|------|
| `vertical` | 默认，新建/编辑页 |
| `horizontal` | 字段少、标签在左 |
| `inline` | 列表顶栏搜索（见 list-page） |

## 配套

| 域 | 内容 |
|----|------|
| `actions.submitForm` | `request` → `message` → `navigate` |
| `dataSources` | 选项类 `static` / `service` |
| 级联字段 | `form.setValues` + `setState` → 见 form 模板 |

## 与弹层表单

短编辑可放抽屉：列表页 `overlay.open` + 独立 `page-template-edit-drawer.yaml` → [overlay.md](../behavior/overlay.md)。
