package com.innospots.nexus.core.server.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 本地服务节点的可变生命周期状态。
 * <p>通过 {@link AtomicBoolean} 与 {@code volatile} 字段保证线程安全，
 * 跟踪节点是否运行、集群键及 Leader 状态。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public class ServiceLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(true);
    @Getter
    @Setter
    private volatile String serverKey;
    @Setter
    private volatile boolean leader;

    /** 服务仍在运行（未关闭）时返回 {@code true}。 */
    public boolean isRunning() {
        return running.get();
    }

    /** 调用 {@link #shutdown()} 后返回 {@code true}。 */
    public boolean isShutdown() {
        return !running.get();
    }

    /** 将服务标记为已停止。 */
    public void shutdown() {
        running.set(false);
    }

    /** 返回集群范围的服务键（{@code host:port}）。 */
    public String serverKey() {
        return serverKey;
    }

    /** 当前节点是否为 Leader 时返回 {@code true}。 */
    public boolean isLeader() {
        return leader;
    }

    /** 已通过注册分配服务键时返回 {@code true}。 */
    public boolean isRegistered() {
        return serverKey != null;
    }
}
