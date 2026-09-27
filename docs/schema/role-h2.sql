-- Canonical H2 DDL for console role domain (see RoleEntity / RoleBindingEntity).
-- Consumed by tests and applications via classpath:nexus/schema/role-h2.sql (sync from this file).

CREATE TABLE IF NOT EXISTS nx_role (
    role_id varchar(32) NOT NULL PRIMARY KEY,
    role_name varchar(64) NOT NULL,
    role_code varchar(64) NOT NULL,
    owner_type varchar(32) NOT NULL,
    owner_id varchar(32),
    security_realm varchar(32) NOT NULL,
    description varchar(256),
    status varchar(32) NOT NULL,
    sort_order integer NOT NULL,
    built_in boolean NOT NULL,
    administrator boolean NOT NULL,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(64),
    updated_by varchar(64),
    CONSTRAINT uk_nx_role_owner_code UNIQUE (owner_type, owner_id, security_realm, role_code)
);

CREATE INDEX IF NOT EXISTS idx_nx_role_owner_status
    ON nx_role (owner_type, owner_id, status);
CREATE INDEX IF NOT EXISTS idx_nx_role_name
    ON nx_role (role_name);
CREATE INDEX IF NOT EXISTS idx_nx_role_realm
    ON nx_role (security_realm);

CREATE TABLE IF NOT EXISTS nx_role_binding (
    binding_id varchar(32) NOT NULL PRIMARY KEY,
    role_id varchar(32) NOT NULL,
    subject_type varchar(32) NOT NULL,
    subject_id varchar(32) NOT NULL,
    owner_type varchar(32) NOT NULL,
    owner_id varchar(32),
    security_realm varchar(32) NOT NULL,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(64),
    updated_by varchar(64),
    CONSTRAINT uk_nx_role_binding_subject UNIQUE (
        owner_type, owner_id, security_realm, role_id, subject_type, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_role_binding_subject
    ON nx_role_binding (subject_type, subject_id);
CREATE INDEX IF NOT EXISTS idx_nx_role_binding_role
    ON nx_role_binding (role_id);
