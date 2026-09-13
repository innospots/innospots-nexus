# Package `domain.condition`

## DatabaseFactorStatement

**Type:** class

Renders a `Factor` into a SQL expression string.

## EmbedCondition

**Type:** class

A condition that supports nesting — each `EmbedCondition` can contain child sub-conditions recursively.

## Factor

**Type:** class

A single filter criterion composed of a field code, an operator, a value, and an optional value type.

## FactorStatementBuilder

**Type:** class

Factory that selects the appropriate `IFactorStatement` implementation based on the target `Mode`.

## IFactorStatement

**Type:** interface

Strategy interface for rendering a `Factor` into a mode-specific expression string (SQL, script, or Java).

## Mode

**Type:** enum

The target output mode for condition statements.

## Operator

**Type:** enum

Comparison operators used in filter conditions.

## Relation

**Type:** enum

Logical combinators for joining multiple `Factor` conditions.

## ScriptFactorStatement

**Type:** class

Renders a `Factor` into a script/expression language (e.

## SimpleCondition

**Type:** class

A flat list of `Factor` conditions joined by a single `Relation`.
