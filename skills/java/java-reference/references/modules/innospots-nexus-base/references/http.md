# 包 `http`

## HttpClientBuilder

**类型：** class

CloseableHttpClient 的流式构建器，提供合理默认值 （连接超时 10s、响应超时 30s、启用重定向）。

### 方法

#### `create() → HttpClientBuilder`
- **说明：** 创建构建器实例。
- **返回：** 构建器

#### `connectTimeout(Duration connectTimeout) → HttpClientBuilder`
- **说明：** 设置连接超时。为 null 时回退到默认值（10s）。
- **参数：**
  - `connectTimeout` — 连接超时
- **返回：** 当前构建器

#### `responseTimeout(Duration responseTimeout) → HttpClientBuilder`
- **说明：** 设置响应/套接字超时。为 null 时回退到默认值（30s）。
- **参数：**
  - `responseTimeout` — 响应超时
- **返回：** 当前构建器

#### `redirectsEnabled(boolean redirectsEnabled) → HttpClientBuilder`
- **说明：** 启用或禁用 HTTP 重定向跟随（默认：启用）。
- **参数：**
  - `redirectsEnabled` — 是否跟随重定向
- **返回：** 当前构建器

#### `userAgent(String userAgent) → HttpClientBuilder`
- **说明：** 设置每个请求发送的 User-Agent 请求头。
- **参数：**
  - `userAgent` — User-Agent 值
- **返回：** 当前构建器

#### `defaultHeader(String name, String value) → HttpClientBuilder`
- **说明：** 添加每个请求默认发送的请求头。
- **参数：**
  - `name` — 请求头名称
  - `value` — 请求头值
- **返回：** 当前构建器

#### `build() → CloseableHttpClient`
- **说明：** 按配置构建 CloseableHttpClient。 使用带单一连接配置的池化连接管理器。
- **返回：** HTTP 客户端


## HttpResult

**类型：** record

HTTP 请求结果的不可变记录。包含状态码、原因短语、响应体与响应头。

### 组件（record）

| 名称 | 类型 | 说明 |
|------|------|------|
| `statusCode` | `int` | HTTP 状态码 |
| `reasonPhrase` | `String` | 原因短语 |
| `body` | `String` | 响应体 |
| `headers` | `Map<String, List<String>>` | 响应头（多值） |

### 方法

#### `isSuccessful() → boolean`
- **说明：** 判断响应是否成功（状态码 2xx）。
- **返回：** 成功时返回 true


## HttpUtils

**类型：** class

HTTP GET 与 POST（JSON）请求的便捷方法。 使用 HttpClientBuilder 为每次调用创建短生命周期客户端。

### 方法

#### `get(String url) → HttpResult`
- **说明：** 发送 GET 请求（无自定义请求头）。
- **参数：**
  - `url` — 请求 URL
- **返回：** HTTP 结果

#### `get(String url, Map<String, String> headers) → HttpResult`
- **说明：** 发送带自定义请求头的 GET 请求。
- **参数：**
  - `url` — 请求 URL
  - `headers` — 请求头
- **返回：** HTTP 结果

#### `get(CloseableHttpClient client, String url) → HttpResult`
- **说明：** 使用已有客户端发送 GET 请求（无自定义请求头）。
- **参数：**
  - `client` — HTTP 客户端
  - `url` — 请求 URL
- **返回：** HTTP 结果

#### `get(CloseableHttpClient client, String url, Map<String, String> headers) → HttpResult`
- **说明：** 使用已有客户端发送带自定义请求头的 GET 请求。
- **参数：**
  - `client` — HTTP 客户端
  - `url` — 请求 URL
  - `headers` — 请求头
- **返回：** HTTP 结果

#### `postJson(String url, String jsonBody) → HttpResult`
- **说明：** 发送 JSON POST 请求（无自定义请求头）。
- **参数：**
  - `url` — 请求 URL
  - `jsonBody` — JSON 请求体
- **返回：** HTTP 结果

#### `postJson(String url, String jsonBody, Map<String, String> headers) → HttpResult`
- **说明：** 发送带自定义请求头的 JSON POST 请求。
- **参数：**
  - `url` — 请求 URL
  - `jsonBody` — JSON 请求体
  - `headers` — 请求头
- **返回：** HTTP 结果

#### `postJson(CloseableHttpClient client,
            String url,
            String jsonBody,
            Map<String, String> headers) → HttpResult`
- **说明：** 使用已有客户端发送 JSON POST 请求。
- **参数：**
  - `client` — HTTP 客户端
  - `url` — 请求 URL
  - `jsonBody` — JSON 请求体
  - `headers` — 请求头
- **返回：** HTTP 结果

#### `execute(CloseableHttpClient client, org.apache.hc.core5.http.ClassicHttpRequest request) → HttpResult`
- **说明：** 使用已有客户端执行 HTTP 请求。
- **参数：**
  - `client` — HTTP 客户端
  - `request` — HTTP 请求
- **返回：** HTTP 结果
