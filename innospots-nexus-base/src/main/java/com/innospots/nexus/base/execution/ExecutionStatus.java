package com.innospots.nexus.base.execution;

/**
 * 执行从创建到完成的生命周期状态。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ExecutionRecord
 */
public enum ExecutionStatus {
    /** 已创建 */
    CREATED,
    /** 启动中 */
    STARTING,
    /** 就绪 */
    READY,
    /** 等待中 */
    PENDING,
    /** 运行中 */
    RUNNING,
    /** 停止中 */
    STOPPING,
    /** 已停止 */
    STOPPED,
    /** 成功 */
    SUCCESS,
    /** 失败 */
    FAILED
}
