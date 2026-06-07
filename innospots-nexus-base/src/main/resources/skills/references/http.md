# HTTP (`com.innospots.nexus.base.http`)

## HttpClientBuilder

**Type:** final class

Fluent builder for `CloseableHttpClient` instances with sensible defaults (10s connect timeout, 30s response timeout, redirects enabled).

### Static Method
- **Signature:** `create() → HttpClientBuilder`
- **Description:** Creates a new builder instance with default settings.
- **Parameters:** none
- **Returns:** `HttpClientBuilder`

### Method
- **Signature:** `connectTimeout(Duration connectTimeout) → HttpClientBuilder`
- **Description:** Sets the connection timeout. Falls back to default (10s) if null.
- **Parameters:** `connectTimeout` — connection timeout duration
- **Returns:** `HttpClientBuilder` — this instance (fluent)

### Method
- **Signature:** `responseTimeout(Duration responseTimeout) → HttpClientBuilder`
- **Description:** Sets the response/socket timeout. Falls back to default (30s) if null.
- **Parameters:** `responseTimeout` — response timeout duration
- **Returns:** `HttpClientBuilder` — this instance (fluent)

### Method
- **Signature:** `redirectsEnabled(boolean redirectsEnabled) → HttpClientBuilder`
- **Description:** Enables or disables HTTP redirect following (default: enabled).
- **Parameters:** `redirectsEnabled` — whether to follow redirects
- **Returns:** `HttpClientBuilder` — this instance (fluent)

### Method
- **Signature:** `userAgent(String userAgent) → HttpClientBuilder`
- **Description:** Sets the User-Agent header to be sent with every request.
- **Parameters:** `userAgent` — the user agent string
- **Returns:** `HttpClientBuilder` — this instance (fluent)

### Method
- **Signature:** `defaultHeader(String name, String value) → HttpClientBuilder`
- **Description:** Adds a default header to be sent with every request. Null values are ignored.
- **Parameters:** `name` — header name; `value` — header value
- **Returns:** `HttpClientBuilder` — this instance (fluent)

### Method
- **Signature:** `build() → CloseableHttpClient`
- **Description:** Builds a `CloseableHttpClient` with the configured settings. Uses a pooled connection manager.
- **Parameters:** none
- **Returns:** `CloseableHttpClient`

---

## HttpResult

**Type:** record

Immutable result of an HTTP request. Contains the status code, reason phrase, response body, and response headers.

### Record Components
- **Signature:** `statusCode() → int`
- **Description:** HTTP status code (e.g. 200, 404, 500).
- **Signature:** `reasonPhrase() → String`
- **Description:** HTTP reason phrase (e.g. "OK", "Not Found").
- **Signature:** `body() → String`
- **Description:** Response body as a string.
- **Signature:** `headers() → Map<String, List<String>>`
- **Description:** Response headers as a multi-map.

### Method
- **Signature:** `isSuccessful() → boolean`
- **Description:** Returns true if the status code is in the 2xx range.
- **Parameters:** none
- **Returns:** `boolean`

---

## HttpUtils

**Type:** final class

Convenience methods for HTTP GET and POST (JSON) requests. Uses `HttpClientBuilder` to create short-lived clients for each call.

### Static Method
- **Signature:** `get(String url) → HttpResult`
- **Description:** Performs an HTTP GET request.
- **Parameters:** `url` — the target URL
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `get(String url, Map<String, String> headers) → HttpResult`
- **Description:** Performs an HTTP GET request with custom headers.
- **Parameters:** `url` — the target URL; `headers` — request headers map
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `get(CloseableHttpClient client, String url) → HttpResult`
- **Description:** Performs an HTTP GET request using a pre-configured client.
- **Parameters:** `client` — a `CloseableHttpClient` instance; `url` — the target URL
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `get(CloseableHttpClient client, String url, Map<String, String> headers) → HttpResult`
- **Description:** Performs an HTTP GET request using a pre-configured client and custom headers.
- **Parameters:** `client` — a `CloseableHttpClient` instance; `url` — the target URL; `headers` — request headers map
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `postJson(String url, String jsonBody) → HttpResult`
- **Description:** Performs an HTTP POST with a JSON body.
- **Parameters:** `url` — the target URL; `jsonBody` — JSON string body
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `postJson(String url, String jsonBody, Map<String, String> headers) → HttpResult`
- **Description:** Performs an HTTP POST with a JSON body and custom headers.
- **Parameters:** `url` — the target URL; `jsonBody` — JSON string body; `headers` — request headers map
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `postJson(CloseableHttpClient client, String url, String jsonBody, Map<String, String> headers) → HttpResult`
- **Description:** Performs an HTTP POST with a JSON body using a pre-configured client.
- **Parameters:** `client` — a `CloseableHttpClient` instance; `url` — the target URL; `jsonBody` — JSON string body; `headers` — request headers map
- **Returns:** `HttpResult`
- **Throws:** `IOException`

### Static Method
- **Signature:** `execute(CloseableHttpClient client, ClassicHttpRequest request) → HttpResult`
- **Description:** Executes a generic HTTP request and converts the response to `HttpResult`.
- **Parameters:** `client` — a `CloseableHttpClient` instance; `request` — an `ClassicHttpRequest`
- **Returns:** `HttpResult`
- **Throws:** `IOException`