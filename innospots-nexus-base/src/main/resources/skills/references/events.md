# Events (`com.innospots.nexus.base.events`)

## DomainEvent

**Type:** interface

Base interface for all domain events. Each event has a unique ID, a type identifier, and a timestamp of occurrence.

### Method
- **Signature:** `eventId() → String`
- **Description:** Returns a UUID string generated randomly. Default method.
- **Parameters:** none
- **Returns:** `String` — UUID-based event identifier

### Method
- **Signature:** `eventType() → String`
- **Description:** Returns the event type identifier. Must be implemented by subclasses.
- **Parameters:** none
- **Returns:** `String` — event type name

### Method
- **Signature:** `occurredAt() → Instant`
- **Description:** Returns the current instant when called. Default method.
- **Parameters:** none
- **Returns:** `Instant` — timestamp of occurrence

---

## EventHandler

**Type:** `@FunctionalInterface`

Functional interface for domain event handlers.

### Method
- **Signature:** `handle(E event) → Object`
- **Description:** Handles the event and optionally returns a result.
- **Parameters:** `E event` (extends `DomainEvent`) — the event to process
- **Returns:** `Object` — handler result (may be null)

---

## EventBus

**Type:** final class

Simple in-memory event bus. Subscribers register via `subscribe` for specific event types; publishers fire events via `publish` (fire-and-forget) or `publishSync` (blocking, returns last handler result). Handlers are stored in `CopyOnWriteArrayList` for safe concurrent iteration. Event-type matching uses `Class.isAssignableFrom` so handlers match subclasses of the registered type.

### Method
- **Signature:** `subscribe(Class<E> eventType, EventHandler<E> handler) → void`
- **Description:** Registers a handler for a specific event type.
- **Parameters:** `eventType` — the event class to subscribe to; `handler` — the handler to invoke
- **Returns:** void

### Method
- **Signature:** `unsubscribe(Class<E> eventType, EventHandler<E> handler) → boolean`
- **Description:** Removes a previously registered handler.
- **Parameters:** `eventType` — the event class; `handler` — the handler to remove
- **Returns:** `boolean` — true if the handler was found and removed

### Method
- **Signature:** `publish(DomainEvent event) → void`
- **Description:** Fires an event to all matching handlers asynchronously (fire-and-forget).
- **Parameters:** `event` — the event to publish
- **Returns:** void

### Method
- **Signature:** `publishSync(DomainEvent event) → Object`
- **Description:** Fires an event synchronously, returning the last handler's result.
- **Parameters:** `event` — the event to publish
- **Returns:** `Object` — result from the last matching handler

### Method
- **Signature:** `clear() → void`
- **Description:** Removes all registered handlers.
- **Parameters:** none
- **Returns:** void