package com.innospots.nexus.base.domain.data;

/**
 * 对目标数据源执行的数据操作类型。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DataRequest
 */
public enum DataOperation {
    /** 查询 */
    QUERY,
    /** 创建 */
    CREATE,
    /** 更新 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 执行 */
    EXECUTE
}
