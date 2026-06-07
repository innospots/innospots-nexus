# Execution (`com.innospots.nexus.base.execution`)

## ExecutionStatus

**Type:** enum

Lifecycle states for an execution from creation through to completion.

### Values
- `CREATED` — execution has been created
- `STARTING` — execution is starting up
- `READY` — execution is ready to run
- `PENDING` — execution is pending
- `RUNNING` — execution is actively running
- `STOPPING` — execution is in the process of stopping
- `STOPPED` — execution has been stopped
- `SUCCESS` — execution completed successfully
- `FAILED` — execution has failed

---

## Executor

**Type:** `interface`

Core execution unit interface. Each executor has a unique identifier and is invoked with an `ExecutionContext` to produce an output.

### Method
- **Signature:** `identifier() → String`
- **Description:** Returns the unique identifier for this executor.
- **Parameters:** none
- **Returns:** `String`

### Method
- **Signature:** `execute(C context) → O`
- **Description:** Executes the logic with the given context and returns the output.
- **Parameters:** `context` (extends `ExecutionContext`) — the execution context
- **Returns:** `O` — the output value

### Method
- **Signature:** `info() → String`
- **Description:** Returns informational text about the executor. Defaults to `identifier()`.
- **Parameters:** none
- **Returns:** `String` — informational description

---

## ExecutionContext

**Type:** class

Execution context for a single run. Contains immutable input parameters (`inputs`) and a mutable working memory (`context`) that executors can read and write during execution.

### Static Method
- **Signature:** `create(String executionId) → ExecutionContext`
- **Description:** Creates a context with the given execution ID and empty inputs/context maps.
- **Parameters:** `executionId` — unique identifier for this execution
- **Returns:** `ExecutionContext`

### Method
- **Signature:** `executionId() → String`
- **Description:** Returns the execution identifier.
- **Parameters:** none
- **Returns:** `String`

### Method
- **Signature:** `input(String key, Object value) → ExecutionContext`
- **Description:** Sets an input parameter. Inputs are part of the immutable execution boundary.
- **Parameters:** `key` — parameter name; `value` — parameter value
- **Returns:** `ExecutionContext` — this instance (fluent)

### Method
- **Signature:** `getInput(String key) → Object`
- **Description:** Gets an input parameter by key.
- **Parameters:** `key` — parameter name
- **Returns:** `Object` — the input value, or null

### Method
- **Signature:** `getInputString(String key) → String`
- **Description:** Gets an input parameter as a String, or null.
- **Parameters:** `key` — parameter name
- **Returns:** `String` — string representation of the value, or null

### Method
- **Signature:** `getInputInteger(String key) → Integer`
- **Description:** Gets an input parameter as an Integer, or null.
- **Parameters:** `key` — parameter name
- **Returns:** `Integer` — converted integer value, or null

### Method
- **Signature:** `getInputLong(String key) → Long`
- **Description:** Gets an input parameter as a Long, or null.
- **Parameters:** `key` — parameter name
- **Returns:** `Long` — converted long value, or null

### Method
- **Signature:** `inputs() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of all input parameters.
- **Parameters:** none
- **Returns:** `Map<String, Object>`

### Method
- **Signature:** `put(String key, Object value) → ExecutionContext`
- **Description:** Puts a value into the mutable working context. Unlike inputs, context can be read and written by executors during execution.
- **Parameters:** `key` — context key; `value` — context value
- **Returns:** `ExecutionContext` — this instance (fluent)

### Method
- **Signature:** `get(String key) → Object`
- **Description:** Gets a context value by key.
- **Parameters:** `key` — context key
- **Returns:** `Object` — the context value, or null

### Method
- **Signature:** `getString(String key) → String`
- **Description:** Gets a context value as a String, or null.
- **Parameters:** `key` — context key
- **Returns:** `String` — string representation, or null

### Method
- **Signature:** `getInteger(String key) → Integer`
- **Description:** Gets a context value as an Integer, or null.
- **Parameters:** `key` — context key
- **Returns:** `Integer` — converted integer value, or null

### Method
- **Signature:** `getLong(String key) → Long`
- **Description:** Gets a context value as a Long, or null.
- **Parameters:** `key` — context key
- **Returns:** `Long` — converted long value, or null

### Method
- **Signature:** `context() → Map<String, Object>`
- **Description:** Returns an unmodifiable view of the mutable context map.
- **Parameters:** none
- **Returns:** `Map<String, Object>`

---

## ExecutionRecord

**Type:** class (uses `@Getter`)

Immutable record of a completed execution. Captures the execution ID, executor identity, status, timing, context snapshot, output, and any status message.

### Constructor
- **Signature:** `ExecutionRecord(String executionId, String executorId, ExecutionStatus status, LocalDateTime startTime, LocalDateTime endTime, Map<String, Object> context, Map<String, Object> output, String message)`
- **Description:** Creates an immutable execution record. Null maps default to empty unmodifiable maps.
- **Parameters:** `executionId` — unique execution identifier; `executorId` — executor identifier; `status` — final status; `startTime` — start timestamp; `endTime` — end timestamp; `context` — snapshot of the context map; `output` — output data map; `message` — status message

### Accessor Methods (all generated by `@Getter`)
- **Signature:** `executionId() → String`
- **Signature:** `executorId() → String`
- **Signature:** `status() → ExecutionStatus`
- **Signature:** `startTime() → LocalDateTime`
- **Signature:** `endTime() → LocalDateTime`
- **Signature:** `context() → Map<String, Object>`
- **Signature:** `output() → Map<String, Object>`
- **Signature:** `message() → String`