# Domain Dictionary

## DictionaryItem

**Type:** record

A single key-value entry within a dictionary type. The display name and type name are internationalized (`I18nObject`).

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `value` | `String` | The stored value/key of the dictionary entry |
| `name` | `I18nObject` | Internationalized display name of the entry |
| `type` | `String` | The type/category this item belongs to |
| `typeName` | `I18nObject` | Internationalized display name of the type |
| `status` | `BasicStatus` | Enable/disable status of the entry |

---

## DictionaryType

**Type:** record

A dictionary type (e.g. "gender", "country") that groups related `DictionaryItem` entries. The display name is internationalized.

### Record Components

| Component | Type | Description |
|-----------|------|-------------|
| `code` | `String` | Programmatic identifier for the dictionary type |
| `name` | `I18nObject` | Internationalized display name |
| `status` | `BasicStatus` | Enable/disable status of the type |