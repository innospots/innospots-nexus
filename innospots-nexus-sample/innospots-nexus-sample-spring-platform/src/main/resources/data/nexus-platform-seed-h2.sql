-- Platform sample 演示数据（DML，local 启动时于 DDL 之后执行）。
--
-- 演示主键：实体 idPrefix + 11 位（共 14 字符）；库列仍为 VARCHAR(32)，运行时 ID 仍由生成器写入。
-- 密码：nx_user_credential.verifier 为 BCrypt（bcrypt@v1）；登录 API 传 RSA 密文，见 nexus/console-auth.yaml。
-- 联调明文：Admin12345 / User12345 / Opt12345。

-- 按演示用户 subject_id 清理全部绑定（集成测试可能追加其它 binding_id）
DELETE FROM nx_role_binding
WHERE subject_type = 'USER'
  AND subject_id IN ('PUS00000000001', 'PUS00000000002', 'PUS00000000003');
DELETE FROM nx_user_credential WHERE credential_id IN ('UCR00000000001', 'UCR00000000002', 'UCR00000000003');
DELETE FROM nx_admin_user WHERE admin_user_id IN ('PUS00000000001', 'PUS00000000002', 'PUS00000000003');
DELETE FROM nx_role WHERE role_id IN ('ROL00000000001', 'ROL00000000002', 'ROL00000000003');

INSERT INTO nx_role (
    role_id, role_name, role_code, owner_type, owner_id, security_realm,
    description, status, sort_order, built_in, administrator, created_at, updated_at
) VALUES
    (
        'ROL00000000001', '系统管理员', 'sys_admin',
        'PLATFORM', NULL, 'PLATFORM', '平台内置系统管理员', 'ENABLED', 10, TRUE, TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'ROL00000000002', '系统运营', 'operator',
        'PLATFORM', NULL, 'PLATFORM', '平台内置运营角色', 'ENABLED', 20, TRUE, FALSE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'ROL00000000003', '成员', 'user',
        'PLATFORM', NULL, 'PLATFORM', '平台内置普通成员', 'ENABLED', 30, TRUE, FALSE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );

INSERT INTO nx_admin_user (
    admin_user_id, security_realm, login_name, display_name, email, status, created_at, updated_at
) VALUES
    (
        'PUS00000000001', 'PLATFORM', 'admin', 'Admin', 'admin@demo.com', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'PUS00000000002', 'PLATFORM', 'user01', 'User01', 'user01@demo.com', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'PUS00000000003', 'PLATFORM', 'opt01', 'Opt01', 'opt01@demo.com', 'ACTIVE',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );

INSERT INTO nx_user_credential (
    credential_id, subject_id, credential_kind, algorithm, verifier, verifier_params,
    credential_version, force_reset, failed_attempts, owner_type, owner_id, security_realm,
    created_at, updated_at
) VALUES
    (
        'UCR00000000001', 'PUS00000000001', 'PASSWORD', 'bcrypt@v1',
        '$2a$10$abcdefghijklmnopqrstuugztTh22ZYjI5SQsQOFxilLg44zef6aO', NULL,
        1, FALSE, 0, 'PLATFORM', NULL, 'PLATFORM',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'UCR00000000002', 'PUS00000000002', 'PASSWORD', 'bcrypt@v1',
        '$2a$10$abcdefghijklmnopqrstuuWut1jz6.MvEjp/RelaexWHd/FHnMk1O', NULL,
        1, FALSE, 0, 'PLATFORM', NULL, 'PLATFORM',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'UCR00000000003', 'PUS00000000003', 'PASSWORD', 'bcrypt@v1',
        '$2a$10$abcdefghijklmnopqrstuuuEwMq67UYGbxxjZ6Y00NmpfbX8Tnb1K', NULL,
        1, FALSE, 0, 'PLATFORM', NULL, 'PLATFORM',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );

INSERT INTO nx_role_binding (
    binding_id, role_id, subject_type, subject_id, owner_type, owner_id, security_realm,
    created_at, updated_at
) VALUES
    (
        'RBN00000000001', 'ROL00000000001', 'USER', 'PUS00000000001', 'PLATFORM', NULL, 'PLATFORM',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'RBN00000000002', 'ROL00000000003', 'USER', 'PUS00000000002', 'PLATFORM', NULL, 'PLATFORM',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    ),
    (
        'RBN00000000003', 'ROL00000000002', 'USER', 'PUS00000000003', 'PLATFORM', NULL, 'PLATFORM',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
    );

-- 角色资源授权见 data/nexus-platform-seed-permission-grants-h2.sql（local profile 在 spring.sql.init 中紧随本脚本执行）。
