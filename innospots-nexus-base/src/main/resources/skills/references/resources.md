# Resources — File & Resource Store Abstractions

## FileResource

**Type:** record

A file resource with its content stream and metadata flags.

### Components
- `String name` — logical resource name
- `String fileName` — original file name
- `String contentType` — MIME type
- `InputStream inputStream` — file content stream
- `boolean saveMeta` — if true, persist metadata alongside binary content

---

## MetaResource

**Type:** record

Immutable metadata record for a stored resource.

### Components
- `String resourceId` — unique resource identifier
- `String resourceName` — display name
- `String mimeType` — MIME type
- `String module` — owning module
- `String moduleKey` — module-specific key
- `long fileSize` — file size in bytes
- `String fileUri` — storage URI
- `String storeMode` — storage mode identifier
- `Instant createdAt` — creation timestamp

---

## ResourceEvent

**Type:** record implementing `DomainEvent`

Domain event published when resource metadata is saved/persisted.

### Components
- `MetaResource metaResource` — the persisted resource metadata

### Method
- **Signature:** `eventType() → String`
- **Description:** Returns the event type string `"resource.meta.saved"`.

---

## ResourceStore

**Type:** interface

Abstraction for persisting and retrieving binary resources.

### Methods
- **Signature:** `save(FileResource resource, String module, String moduleKey) → MetaResource`
- **Description:** Persists a file resource within a module context.

- **Signature:** `read(String resourceId) → Optional<byte[]>`
- **Description:** Reads a resource's binary content by ID.

- **Signature:** `delete(String resourceId) → boolean`
- **Description:** Deletes a resource by ID.

- **Signature:** `exists(String resourceId) → boolean`
- **Description:** Checks whether a resource exists.

- **Signature:** `storeMode() → String`
- **Description:** Returns a unique store mode identifier (e.g. `"local"`, `"s3"`).

---

## ResourcePatternResolver

**Type:** class

Ant-style classpath resource scanner supporting `classpath*:` and `classpath:` prefixes.
Wraps matched resources as Hutool `Resource` objects backed by `UrlResource`.
The `MatchedResource` inner class preserves the original classpath matching path.

### Constructors

- **Signature:** `ResourcePatternResolver()`
- **Description:** Creates a resolver using the default class loader.

- **Signature:** `ResourcePatternResolver(ClassLoader classLoader)`
- **Description:** Creates a resolver with the specified class loader. Falls back to default if null.

### Methods

- **Signature:** `getResources(String locationPattern) → List<Resource>`
- **Description:** Returns a list of Hutool `Resource` objects matching the given location pattern. The matched path information is discarded; use `getMatchedResources()` if the path is needed.
- **Parameters:** `locationPattern` — Ant-style pattern with optional `classpath*:` or `classpath:` prefix
- **Returns:** list of matching resources (never null)
- **Throws:** `IOException` if classpath scanning fails

- **Signature:** `getMatchedResources(String locationPattern) → List<MatchedResource>`
- **Description:** Returns a list of `MatchedResource` objects. Unlike `getResources()`, each result retains the original matched classpath path.
- **Parameters:** `locationPattern` — Ant-style pattern with optional prefix
- **Returns:** list of matched resources with path info (never null)
- **Throws:** `IOException` if classpath scanning fails

### Inner Class: MatchedResource

**Type:** class implementing `cn.hutool.core.io.resource.Resource`

A matched resource that preserves the original classpath path in addition to providing full `Resource` access via delegation to an internal `UrlResource`.

#### Methods

- **Signature:** `getPath() → String`
- **Description:** Returns the classpath-relative matching path.

- **Signature:** `getResource() → Resource`
- **Description:** Returns the underlying delegate resource (a `UrlResource`).

- **Signature:** `getName() → String` *(from Resource interface)*
- **Description:** Returns the file name extracted from the path.

- **Signature:** `getUrl() → URL` *(from Resource interface)*
- **Description:** Returns the URL of the matched resource.

- **Signature:** `getStream() → InputStream` *(from Resource interface)*
- **Description:** Opens an InputStream to read the resource content.

- **Signature:** `isModified() → boolean` *(from Resource interface)*
- **Description:** Delegates to the underlying resource's modification check.

- **Signature:** `readUtf8Str() → String` *(default from Resource interface)*
- **Description:** Reads the entire resource content as a UTF-8 string.

- **Signature:** `readBytes() → byte[]` *(default from Resource interface)*
- **Description:** Reads the entire resource content as a byte array.

### Usage Examples

```java
ResourcePatternResolver resolver = new ResourcePatternResolver();

// External API — returns standard Resource
List<Resource> resources = resolver.getResources("classpath*:mapper/**/*.xml");
for (Resource r : resources) {
    System.out.println(r.getName() + ": " + r.readUtf8Str());
}

// Advanced — also get matched path
List<MatchedResource> matched = resolver.getMatchedResources("classpath*:mapper/**/*.xml");
for (MatchedResource m : matched) {
    System.out.println(m.getPath());
    System.out.println(m.getUrl());
}
```