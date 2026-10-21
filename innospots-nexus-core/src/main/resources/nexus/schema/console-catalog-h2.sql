CREATE TABLE IF NOT EXISTS nx_console_catalog_resource (
    resource_id varchar(32) NOT NULL PRIMARY KEY,
    owner_plugin_id varchar(256) NOT NULL,
    domain_key varchar(128) NOT NULL,
    module_key varchar(128) NOT NULL,
    resource_type varchar(32) NOT NULL,
    resource_key varchar(256) NOT NULL,
    parent_resource_id varchar(32),
    entry_page_key varchar(256),
    display_name TEXT,
    description TEXT,
    sort_order integer NOT NULL,
    icon varchar(128),
    view_groups TEXT,
    operation_groups TEXT,
    status varchar(32) NOT NULL,
    security_realm varchar(32) NOT NULL,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(64),
    updated_by varchar(64),
    CONSTRAINT uk_nx_console_catalog_resource_key UNIQUE (resource_key)
);

CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_source
    ON nx_console_catalog_resource (owner_plugin_id, module_key, resource_type, status);
CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_parent
    ON nx_console_catalog_resource (parent_resource_id, sort_order);
CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_realm
    ON nx_console_catalog_resource (security_realm);
