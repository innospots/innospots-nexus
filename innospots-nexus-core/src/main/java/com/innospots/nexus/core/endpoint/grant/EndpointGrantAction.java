package com.innospots.nexus.core.endpoint.grant;

/**
 * Endpoint 授权与审计的动作分类。
 *
 * <p>与 HTTP 动词无严格一一对应；契约应选<strong>最具体</strong>的常量。
 * {@link #WRITE} 仅作无法细分的写操作兜底，新接口优先使用 {@link #CREATE}、{@link #DELETE} 等。</p>
 */
public enum EndpointGrantAction {

    /** 查询、分页、详情、选项等只读访问。 */
    READ,

    /** 创建新资源（通常为 POST）。 */
    CREATE,

    /** 更新资源主体字段（通常为 PUT/PATCH）。 */
    UPDATE,

    /** 删除资源（通常为 DELETE）。 */
    DELETE,

    /** 启停、审批状态等生命周期或状态字段变更。 */
    STATUS,

    /** 全量替换（如授权集合、配置覆盖式 PUT）。 */
    REPLACE,

    /** 执行类命令（同步目录、安装插件、重试任务等非典型 CRUD）。 */
    EXECUTE,

    /** 导出、下载、生成报表等向外取数。 */
    EXPORT,

    /** 导入、上传、批量灌入。 */
    IMPORT,

    /** 绑定、授予（成员、角色、权限等建立关联）。 */
    ASSIGN,

    /** 解除绑定、回收、撤销授予。 */
    REVOKE,

    /**
     * 泛化写操作；存量契约兼容用。新 {@link EndpointGrant} 应优先使用更具体的动作。
     */
    WRITE
}
