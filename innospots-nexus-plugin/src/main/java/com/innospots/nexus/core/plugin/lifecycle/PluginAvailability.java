package com.innospots.nexus.core.plugin.lifecycle;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Capability 与 Contribution 共用的原子可用性门控。
 *
 * <p>资源可以先完成内部提交，但只有激活门控后才允许外部查询。</p>
 */
public final class PluginAvailability {

    private final AtomicReference<State> state = new AtomicReference<>(State.UNAVAILABLE);
    private final AtomicLong generation = new AtomicLong();

    /**
     * 返回当前是否允许外部访问插件资源。
     *
     * @return 门控已激活时 {@code true}
     */
    public boolean isActive() {
        return state.get() == State.ACTIVE;
    }

    /**
     * 返回当前资源代次。
     *
     * @return 最近一次成功激活后的代次；未激活时为 {@code 0}
     */
    public long generation() {
        return generation.get();
    }

    /**
     * 将本次已提交资源切换为可见。
     *
     * <p>重复激活保持幂等，不递增代次。
     *
     * @return 激活后的资源代次
     */
    public synchronized long activate() {
        if (state.get() == State.ACTIVE) {
            return generation.get();
        }
        long next = generation.incrementAndGet();
        state.set(State.ACTIVE);
        return next;
    }

    /**
     * 关闭门控并使旧代次失效。
     *
     * <p>重复关闭保持幂等，不递减代次。
     */
    public synchronized void deactivate() {
        state.set(State.UNAVAILABLE);
    }

    /**
     * 校验当前门控仍然有效。
     *
     * @throws NexusException 门控未激活时
     */
    public void requireActive() {
        if (!isActive()) {
            throw NexusException.build(PluginStatusCode.CAPABILITY_NOT_FOUND,
                    "plugin resources are not currently available");
        }
    }

    private enum State {
        ACTIVE,
        UNAVAILABLE
    }
}
