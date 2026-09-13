# Package `thread`

## AsyncExecutors

**Type:** class

Global async executor facade backed by a singleton `NexusThreadPoolExecutor`.

## NexusThreadFactory

**Type:** class

Named `ThreadFactory` that produces threads with a configurable prefix (default `nexus-worker`) and sequence number.

## NexusThreadPoolExecutor

**Type:** class

Custom `ThreadPoolExecutor` that captures and propagates `TLC` context from the submitting thread to the worker thread.

## TLC

**Type:** class

Thread-Local Context — a typed `ThreadLocal` map for propagating cross-cutting state (trace ID, tenant ID, user ID, workspace ID, etc.

## ThreadPoolBuilder

**Type:** class

Fluent builder for `NexusThreadPoolExecutor` instances.
