# 包 `endpoint.root`

## MainRootEndpoint

**类型：** class（final），`@Path("/")`

站点根路径 `GET /`，307 重定向到配置的文档入口。

### 构造方法

#### `MainRootEndpoint(String rootRedirectPath)`
- **参数：**
  - `rootRedirectPath` — 文档入口路径；空白时回退 `/openapi/ui`

### 端点

#### `GET redirectRoot() → Response`
- **说明：** 返回临时重定向响应
