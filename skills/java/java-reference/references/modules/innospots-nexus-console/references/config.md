# 包 `config`

## ConsoleConstant

**类型：** class（final，常量容器）

控制台模块共享常量。

### 常量

| 常量 | 说明 |
|------|------|
| `API_PREFIX = "/api/d/nexus"` | 控制台管理 REST API 根路径前缀 |
| `PUBLIC_API_PREFIX = "/api/public"` | 公共开放 REST API 根路径前缀（无需鉴权）；运营域、租户域等业务域路径不在此定义，由各业务模块自行维护 |

### 方法

#### `apiPath(String suffix) → String`（static）
- **说明：** 拼接 `API_PREFIX` 下的子路径
- **参数：**
  - `suffix` — 以 `/` 开头的相对路径；空串表示仅前缀
- **返回：** 完整 JAX-RS 路径

#### `publicPath(String suffix) → String`（static）
- **说明：** 拼接 `PUBLIC_API_PREFIX` 下的子路径
- **参数：**
  - `suffix` — 以 `/` 开头的相对路径；空串表示仅前缀
- **返回：** 完整 JAX-RS 路径

## AuthConfig

**类型：** class

控制台认证的令牌签发设置。