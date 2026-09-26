# 包 `domain.request`

## Pagination

**类型：** class

查询请求共享的分页默认值与规范化逻辑。

### 方法

#### `normalizePageNo(long pageNo) → long`
- **说明：** 当 pageNo 至少为 1 时返回该值，否则返回 {@link #DEFAULT_PAGE_NO}。
- **参数：**
  - `pageNo` — 请求的页码
- **返回：** 从 1 开始的页码

#### `normalizePageSize(long pageSize) → long`
- **说明：** 当 pageSize 至少为 1 时返回该值，否则返回 {@link #DEFAULT_PAGE_SIZE}。
- **参数：**
  - `pageSize` — 请求的每页记录数
- **返回：** 正数的每页记录数


## SimpleQueryRequest

**类型：** record

带关键词过滤的分页查询请求。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `input` | `String` | 通用模糊搜索关键词，可为 null |
| `pageNo` | `long` | 从 1 开始的页码，默认 1 |
| `pageSize` | `long` | 每页记录数，默认 20 |
