# 包 `domain.request`

## SimpleQueryRequest

**类型：** record

带关键词过滤的分页查询请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 通用模糊搜索关键词，可为 {@code null} |
| `pageNo` | `long` | 从 1 开始的页码，默认 1 |
| `pageSize` | `long` | 每页记录数，默认 20 |

### 构造方法

#### `SimpleQueryRequest()`
