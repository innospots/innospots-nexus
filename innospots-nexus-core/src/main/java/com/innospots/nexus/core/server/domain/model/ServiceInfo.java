package com.innospots.nexus.core.server.domain.model;

import com.innospots.nexus.core.server.domain.enums.ServiceRole;
import com.innospots.nexus.core.server.domain.enums.ServiceStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 已注册服务节点的流式领域模型。
 * <p>承载身份（服务名 + 实例 ID）、网络地址、角色/状态、分组、自由标签与运行时指标。
 * {@link #serverKey()} 派生为 {@code host:port}，保证集群内唯一。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceRole
 * @see ServiceStatus
 */
public class ServiceInfo {

    private Long serverId;
    private final String serviceName;
    private final String instanceId;
    private String host;
    private int port;
    private ServiceStatus status = ServiceStatus.ONLINE;
    private ServiceRole role = ServiceRole.FOLLOWER;
    private String group = "default";
    private LocalDateTime updatedAt = LocalDateTime.now();
    private final Map<String, String> tags = new LinkedHashMap<>();
    private final Map<String, String> metrics = new LinkedHashMap<>();

    private ServiceInfo(String serviceName, String instanceId) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
    }

    /**
     * 按服务名与实例标识创建服务。
     *
     * @param serviceName 逻辑服务类型名称
     * @param instanceId  唯一实例标识（如 UUID 或主机名）
     * @return 新服务信息
     */
    public static ServiceInfo named(String serviceName, String instanceId) {
        return new ServiceInfo(serviceName, instanceId);
    }

    /** 返回数据库分配的服务标识。 */
    public Long serverId() {
        return serverId;
    }

    /** 设置数据库分配的服务标识并返回自身以支持链式调用。 */
    public ServiceInfo serverId(Long serverId) {
        this.serverId = serverId;
        return this;
    }

    /** 返回逻辑服务类型名称。 */
    public String serviceName() {
        return serviceName;
    }

    /** 返回唯一实例标识。 */
    public String instanceId() {
        return instanceId;
    }

    /** 返回主机地址。 */
    public String host() {
        return host;
    }

    /** 设置主机地址并返回自身以支持链式调用。 */
    public ServiceInfo host(String host) {
        this.host = host;
        return this;
    }

    /** 返回端口号。 */
    public int port() {
        return port;
    }

    /** 设置端口号并返回自身以支持链式调用。 */
    public ServiceInfo port(int port) {
        this.port = port;
        return this;
    }

    /** 返回服务状态。 */
    public ServiceStatus status() {
        return status;
    }

    /** 设置服务状态（{@code null} 时默认为 ONLINE）并返回自身以支持链式调用。 */
    public ServiceInfo status(ServiceStatus status) {
        this.status = status == null ? ServiceStatus.ONLINE : status;
        return this;
    }

    /** 返回服务角色。 */
    public ServiceRole role() {
        return role;
    }

    /** 设置服务角色（{@code null} 时默认为 FOLLOWER）并返回自身以支持链式调用。 */
    public ServiceInfo role(ServiceRole role) {
        this.role = role == null ? ServiceRole.FOLLOWER : role;
        return this;
    }

    /** 返回分组名称。 */
    public String group() {
        return group;
    }

    /** 设置分组名称（{@code null} 时默认为 "default"）并返回自身以支持链式调用。 */
    public ServiceInfo group(String group) {
        this.group = group == null ? "default" : group;
        return this;
    }

    /** 返回最后心跳更新时间。 */
    public LocalDateTime updatedAt() {
        return updatedAt;
    }

    /** 设置最后心跳更新时间并返回自身以支持链式调用。 */
    public ServiceInfo updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /** 集群范围唯一键：{@code host:port}。 */
    public String serverKey() {
        return host + ":" + port;
    }

    /** 返回标签映射的不可变副本。 */
    public Map<String, String> tags() {
        return Map.copyOf(tags);
    }

    /**
     * 从逗号分隔的 {@code key=value} 对解析并设置标签。
     * 示例：{@code "env=prod,region=us-east-1"}。
     *
     * @param tags 标签字符串
     * @return 自身以支持链式调用
     */
    public ServiceInfo tags(String tags) {
        if (tags != null) {
            for (String pair : tags.split(",")) {
                String[] kv = pair.trim().split("=", 2);
                if (kv.length == 2) {
                    this.tags.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        return this;
    }

    /** 返回指标映射的不可变副本。 */
    public Map<String, String> metrics() {
        return Map.copyOf(metrics);
    }

    /** 将给定映射合并到运行时指标中。 */
    public ServiceInfo metrics(Map<String, String> metrics) {
        if (metrics != null) {
            this.metrics.putAll(metrics);
        }
        return this;
    }

    /** 自最后心跳更新起的秒数，用于过期检测。 */
    public long elapsedSecondsSinceUpdate() {
        return updatedAt == null
                ? 0
                : java.time.Duration.between(updatedAt, LocalDateTime.now()).toSeconds();
    }
}
