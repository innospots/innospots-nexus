-- 插件权限资源归属字段迁移参考脚本。
-- 执行前必须完成 extension_key -> owner_plugin_id 的人工身份核对、数据复制和唯一性检查。
-- 本脚本只负责结构迁移，不自动猜测或改写插件身份。

ALTER TABLE nx_permission_resource
    RENAME COLUMN extension_key TO owner_plugin_id;

DROP INDEX idx_nx_permission_resource_source ON nx_permission_resource;

CREATE INDEX idx_nx_permission_resource_source
    ON nx_permission_resource (
        workspace_id,
        owner_plugin_id,
        module_key,
        resource_type,
        status
    );

ALTER TABLE nx_permission_resource
    MODIFY COLUMN owner_plugin_id varchar(256) NOT NULL;

-- 校验归属字段、资源身份和新索引所需的核心数据约束。
SELECT COUNT(*) AS invalid_owner_plugin_count
FROM nx_permission_resource
WHERE owner_plugin_id IS NULL OR TRIM(owner_plugin_id) = '';

SELECT owner_plugin_id, resource_key, COUNT(*) AS duplicate_resource_count
FROM nx_permission_resource
GROUP BY owner_plugin_id, resource_key
HAVING COUNT(*) > 1;
