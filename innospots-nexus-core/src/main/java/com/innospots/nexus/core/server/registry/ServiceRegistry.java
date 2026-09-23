package com.innospots.nexus.core.server.registry;

import com.innospots.nexus.core.server.domain.model.ServiceInfo;
import java.util.List;

/**
 * 服务节点注册表契约，负责注册、注销与查询集群内服务实例。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceInfo
 */
public interface ServiceRegistry {

    /**
     * 在注册表中注册或更新服务节点。
     *
     * @param service 待注册的服务信息
     */
    void register(ServiceInfo service);

    /**
     * 按服务键从注册表移除节点。
     *
     * @param serverKey 唯一服务键（host:port）
     */
    void unregister(String serverKey);

    /**
     * 按服务键查找服务。
     *
     * @param serverKey 待查找的唯一键
     * @return 匹配的服务信息；未找到时返回 {@code null}
     */
    ServiceInfo findByKey(String serverKey);

    /**
     * 列出所有在线服务。
     *
     * @return 状态为 ONLINE 的服务列表
     */
    List<ServiceInfo> listOnline();

    /**
     * 列出所有已注册服务，不区分状态。
     *
     * @return 全部已注册服务
     */
    List<ServiceInfo> listAll();

    /**
     * 统计当前在线服务数量。
     *
     * @return 状态为 ONLINE 的服务数
     */
    int onlineCount();
}
