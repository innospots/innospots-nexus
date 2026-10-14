-- 角色域集成测试最小 DDL（与 nexus-console-h2.sql 中 nx_role* 表一致）
CREATE TABLE IF NOT EXISTS nx_role_binding (
    binding_id    VARCHAR(32) NOT NULL,
    role_id       VARCHAR(32) NOT NULL,
    subject_type  VARCHAR(32) NOT NULL,
    subject_id    VARCHAR(32) NOT NULL,
    owner_type    VARCHAR(32) NOT NULL,
    owner_id      VARCHAR(32),
    security_realm VARCHAR(32) NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    PRIMARY KEY (binding_id),
    CONSTRAINT uk_nx_role_binding_subject UNIQUE (owner_type, owner_id, security_realm, role_id, subject_type, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_role_binding_subject ON nx_role_binding (subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_nx_role_binding_role ON nx_role_binding (role_id);

CREATE TABLE IF NOT EXISTS nx_role (
    role_id         VARCHAR(32)  NOT NULL,
    role_name       VARCHAR(64)  NOT NULL,
    role_code       VARCHAR(64)  NOT NULL,
    owner_type      VARCHAR(32)  NOT NULL,
    owner_id        VARCHAR(32),
    security_realm  VARCHAR(32)  NOT NULL,
    description     VARCHAR(256),
    status          VARCHAR(32)  NOT NULL,
    sort_order      INTEGER          NOT NULL,
    built_in        BOOLEAN   NOT NULL,
    administrator   BOOLEAN   NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (role_id),
    CONSTRAINT uk_nx_role_owner_code UNIQUE (owner_type, owner_id, security_realm, role_code)
);

CREATE INDEX IF NOT EXISTS idx_nx_role_owner_status ON nx_role (owner_type, owner_id, status);

CREATE INDEX IF NOT EXISTS idx_nx_role_name ON nx_role (role_name);

CREATE INDEX IF NOT EXISTS idx_nx_role_realm ON nx_role (security_realm);
