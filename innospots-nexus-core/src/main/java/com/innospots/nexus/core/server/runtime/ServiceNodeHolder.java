package com.innospots.nexus.core.server.runtime;

import com.innospots.nexus.core.server.domain.enums.ServiceRole;
import com.innospots.nexus.core.server.domain.model.ServiceInfo;
import com.innospots.nexus.core.server.domain.model.ServiceLifecycle;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 持有本地节点的服务注册状态，并提供 Leader 判定、心跳失效检测与分片计算。
 * <p>所有状态字段为 {@code volatile}，保证跨线程可见性。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceInfo
 * @see ServiceLifecycle
 */
public class ServiceNodeHolder {

    private final long maxValidSeconds;
    private final ServiceLifecycle lifecycle = new ServiceLifecycle();

    private volatile ServiceInfo currentService;
    private volatile LocalDateTime startupTime;
    private volatile boolean leader;
    @Setter
    private volatile int position = -1;
    @Setter
    private volatile int availableServicesSize;

    /**
     * @param maxValidSeconds 自上次心跳起超过该秒数则视为节点无效
     */
    public ServiceNodeHolder(long maxValidSeconds) {
        this.maxValidSeconds = maxValidSeconds;
    }

    /** 返回本地节点的可变生命周期状态。 */
    public ServiceLifecycle lifecycle() {
        return lifecycle;
    }

    /** 将当前时间记录为节点启动时刻。 */
    public void markStartup() {
        startupTime = LocalDateTime.now();
    }

    /**
     * 返回已记录的启动时间；尚未标记时返回 {@code null}。
     *
     * @return 启动时间
     */
    public LocalDateTime startupTime() {
        return startupTime;
    }

    /**
     * 注册或更新当前节点的服务信息；Leader 状态由服务角色推导。
     *
     * @param service 服务信息
     */
    public void register(ServiceInfo service) {
        this.currentService = service;
        this.leader = service.role() == ServiceRole.LEADER;
        lifecycle.setServerKey(service.serverKey());
        lifecycle.setLeader(this.leader);
    }

    /** 注销当前节点并重置 Leader 状态。 */
    public void unregister() {
        this.currentService = null;
        this.leader = false;
        lifecycle.setServerKey(null);
        lifecycle.setLeader(false);
    }

    /** 当前节点已注册服务时返回 {@code true}。 */
    public boolean isRegistered() {
        return currentService != null;
    }

    /**
     * 返回当前服务信息；未注册时返回 {@code null}。
     *
     * @return 当前服务信息
     */
    public ServiceInfo currentService() {
        return currentService;
    }

    /**
     * 当前节点为 Leader 且心跳仍在有效窗口内。
     *
     * @return 是否为活跃 Leader
     */
    public boolean isLeader() {
        return leader
                && currentService != null
                && currentService.elapsedSecondsSinceUpdate() < maxValidSeconds;
    }

    /**
     * 远程服务心跳超过 {@code maxValidSeconds} 时视为无效。
     *
     * @param service 待检查的服务
     * @return 是否无效
     */
    public boolean isInvalid(ServiceInfo service) {
        return service != null && service.elapsedSecondsSinceUpdate() > maxValidSeconds;
    }

    /**
     * 返回节点在集群中的位置（从 0 起）；未设置时返回 -1。
     *
     * @return 节点位置
     */
    public int position() {
        return position;
    }

    /**
     * 返回集群中可用服务总数。
     *
     * @return 可用服务数
     */
    public int availableServicesSize() {
        return availableServicesSize;
    }

    /**
     * 根据节点在集群中的位置，用向上取整除法计算分配给本节点的分片键范围。
     *
     * @param totalKeys 分片键总数
     * @return 键索引数组；位置未设置或总数为 0 时返回空数组
     */
    public int[] computeShardingKeys(int totalKeys) {
        if (position < 0 || availableServicesSize <= 0 || totalKeys <= 0) {
            return new int[0];
        }
        int perNode = (int) Math.ceil((double) totalKeys / availableServicesSize);
        int start = position * perNode;
        int end = Math.min((position + 1) * perNode, totalKeys);
        if (start >= totalKeys) {
            return new int[0];
        }
        int[] keys = new int[end - start];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = start + i;
        }
        return keys;
    }
}
