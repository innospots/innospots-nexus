# 包 `domain.condition`

## DatabaseFactorStatement

**Type:** class

将 `Factor` 渲染为 SQL 表达式字符串。

## EmbedCondition

**Type:** class

支持嵌套的条件——每个 `EmbedCondition` 可递归包含子条件。

## Factor

**Type:** class

单个过滤条件，由字段编码、运算符、值及可选值类型组成。

## FactorStatementBuilder

**Type:** class

根据目标 `Mode` 选择相应的 `IFactorStatement` 实现的工厂。

## IFactorStatement

**Type:** interface

策略接口，将 `Factor` 渲染为特定模式的表达式字符串（SQL、脚本或 Java）。

## Mode

**Type:** enum

条件语句的目标输出模式。

## Operator

**Type:** enum

过滤条件使用的比较运算符。

## Relation

**Type:** enum

组合多个 `Factor` 条件的逻辑连接符。

## ScriptFactorStatement

**Type:** class

将 `Factor` 渲染为脚本/表达式形式。

## SimpleCondition

**Type:** class

由单一 `Relation` 连接的 `Factor` 平面列表。
