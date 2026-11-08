# Sample Platform 数据库脚本索引

`local` profile 下由 Spring Boot `spring.sql.init` 按序执行；生产环境请改用 Flyway/Liquibase 等等价迁移流程。

## 执行顺序

| 顺序 | 类型 | 路径（classpath） | 说明 |
|------|------|-------------------|------|
| 1 | DDL | `schema/nexus-core-h2.sql` | core 元数据、系统设置、Quartz 等 |
| 2 | DDL | `schema/nexus-plugin-h2.sql` | 插件安装表 |
| 3 | DDL | `schema/nexus-console-h2.sql` | 控制台 catalog、角色、授权、字典、审计、菜单、凭据 |
| 4 | DDL | `schema/nexus-platform-h2.sql` | 平台租户、用户、邀请、注册审批 |
| 5 | YAML | `console/bootstrap/roles.yaml` | 角色定义 + 角色资源授权（启动管线加载，非 SQL） |
| 6 | YAML | `console/bootstrap/users.yaml` | 初始后台用户 + 用户角色绑定（启动管线加载，非 SQL） |

MySQL 权威 DDL 见仓库 `docs/schema/mysql/`。

## 初始化数据不再使用 SQL

角色、角色授权、后台用户与用户角色绑定**不再**由 `data/*.sql` 写入，改由 classpath
YAML 提供，并在服务启动时由 `console.bootstrap` 管线按依赖顺序同步：

```
nx_console_catalog_resource → nx_role → nx_permission_grant → nx_admin_user → nx_role_binding
      (catalog, 200)          (role, 210)    (role-grant, 220)      (user, 230)  (binding, 240)
```

- 顺序固定不可调换：授权校验依赖目录索引，绑定依赖已存在的角色与用户；
- 两个 YAML 均由服务端提供唯一一份，不跨模块合并；文件内 `roleCode` / `loginName` 重复会启动失败；
- 由 YAML 同步的角色与授权写入 `read_only = true`，管理端不可删除/改授权/下线，
  只能改配置文件后重启生效；
- 用户以 `loginName` 判重，已存在则整体跳过（不重置密码、不改角色绑定）。

## DDL 分文件职责

### `nexus-core-h2.sql`

- `nx_meta_resource` — 文件元数据
- `nx_service_registry` — 服务注册
- `nx_system_setting` — 系统设置键值
- Quartz 相关表（若启用）

### `nexus-plugin-h2.sql`

- 插件发现、安装、运行时状态表

### `nexus-console-h2.sql`

- `nx_console_catalog_resource` — console@2 catalog 索引
- `nx_otp_challenge` — OTP 挑战
- `nx_user_credential` — 密码/TOTP 验证材料（**BCrypt verifier，非明文**）
- `nx_dictionary_type` / `nx_dictionary_item`
- `nx_audit_log`
- `nx_menu`
- `nx_permission_grant` — catalog 授权键 `domain.module:grant`
- `nx_role` / `nx_role_binding`

### `nexus-platform-h2.sql`

- `nx_pl_enterprise` / `nx_tenant` / `nx_tenant_admin`
- `nx_admin_user` — 平台用户（login_name、email、status）
- `nx_admin_user_oauth`
- `nx_pl_invite` / `nx_pl_access_request`

## 演示账号（`console/bootstrap/users.yaml`）

| 登录名 | 明文密码（仅联调） | 邮箱 | 角色 code |
|--------|-------------------|------|-----------|
| admin | Admin12345 | admin@demo.com | sys_admin（administrator） |
| user01 | User12345 | user01@demo.com | user（运营管理仅浏览） |
| opt01 | Opt12345 | opt01@demo.com | operator（运营管理读写） |

登录时请求体中的 `password` 须为 **RSA-OAEP 加密后的 Base64**（公钥见 `nexus/console-auth.yaml`），与集成测试 `PlatformSampleRsaPassword` 一致；库内仅存 BCrypt verifier。
