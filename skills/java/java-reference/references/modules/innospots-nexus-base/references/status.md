# 包 `status`

## NexusStatusCode

**Type:** enum

平台级状态码，含双语（EN/ZH）消息与建议，按 `StatusCategory` 分组。

## StatusCategory

**Type:** enum

按关注领域对状态码进行分类。

## StatusCode

**Type:** interface

可组合状态码接口，由 3 字母模块编码、`StatusCategory`（2 位数字）及 4 位本地编码组成。

## StatusCodeRules

**Type:** class

状态码格式校验规则。
