package com.innospots.nexus.service.contract.audit;

import java.util.function.Consumer;

/**
 * 观察 REQUIRED 审计所需的主机事务完成状态。
 *
 * @author Smars
 * @date 2026/09/13
 * @see CommitState
 */
public interface CommitObserver {

    /**
     * 返回当前提交状态。
     *
     * @return 提交状态
     */
    CommitState currentState();

    /**
     * 注册完成后调用的回调。
     *
     * @param callback 完成回调
     */
    void afterCompletion(Consumer<CommitState> callback);
}
