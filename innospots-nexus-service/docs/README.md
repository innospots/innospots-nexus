# 统一服务接入与运行框架 — 设计索引

本目录是 `innospots-nexus-service` 的 L2 技术设计，对应需求书《Java 统一服务接入与运行框架需求说明书》和开发实践规范。按此规格可完成 Maven 增量与 Java 实现；本目录文件不是可运行代码。

阅读顺序：

1. [service-framework-design.md](service-framework-design.md) — 归属、决策锁定（D1–D15）、边界
2. [service-developer-experience-design.md](service-developer-experience-design.md) — 四级用法、注解落点、禁止项
3. [service-contract-design.md](service-contract-design.md) — 类型、SPI、状态码
4. [service-runtime-design.md](service-runtime-design.md) — 调用链、流/WS/治理算法
5. [service-adapter-design.md](service-adapter-design.md) — Spring / Quarkus 装配与配置
6. [service-implementation-design.md](service-implementation-design.md) — 源文件、测试类、里程碑、验收追踪

下一步：先 `java:project` 落地 M0（Quarkus deployment + adapter-test + BOM），再 `java:develop` 按 M1–M8 测试先行。
