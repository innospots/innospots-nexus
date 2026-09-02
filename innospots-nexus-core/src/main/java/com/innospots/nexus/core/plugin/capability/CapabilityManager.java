package com.innospots.nexus.core.plugin.capability;

import java.util.List;
import java.util.Optional;

import com.innospots.nexus.core.plugin.contract.CapabilityProvider;

/**
 * 用于选择活动 Capability Provider 的类型安全读取边界。
 */
public interface CapabilityManager {

    /**
     * 返回一个与请求类型和标签匹配的活动 Provider。
     *
     * @param name Capability 逻辑名称
     * @param majorVersion API 主版本
     * @param requiredTags Provider 必须包含的标签；{@code null} 表示没有显式标签
     * @param <T> Provider 契约类型
     * @return 选中的 Provider
     * @throws com.innospots.nexus.base.exception.NexusException 没有 Provider 或选择存在歧义时抛出
     */
    <T extends CapabilityProvider> T require(String name, int majorVersion, Tags requiredTags);

    /**
     * 返回一个与请求类型和标签匹配的活动 Provider。
     *
     * @param type 包含 Java API 的 Capability 类型
     * @param requiredTags Provider 必须包含的标签；{@code null} 表示没有显式标签
     * @param <T> Provider 契约类型
     * @return 选中的 Provider
     * @throws com.innospots.nexus.base.exception.NexusException 没有 Provider 或选择存在歧义时抛出
     */
    <T extends CapabilityProvider> T require(CapabilityType<T> type, Tags requiredTags);

    /**
     * 查找与请求类型和标签匹配的活动 Provider。
     *
     * @param name Capability 逻辑名称
     * @param majorVersion API 主版本
     * @param requiredTags Provider 必须包含的标签；{@code null} 表示没有显式标签
     * @param <T> Provider 契约类型
     * @return 匹配的 Provider；没有匹配项时返回空 Optional
     * @throws com.innospots.nexus.base.exception.NexusException 选择存在歧义时抛出
     */
    <T extends CapabilityProvider> Optional<T> find(String name, int majorVersion, Tags requiredTags);

    /**
     * 查找与请求类型和标签匹配的活动 Provider。
     *
     * @param type 包含 Java API 的 Capability 类型
     * @param requiredTags Provider 必须包含的标签；{@code null} 表示没有显式标签
     * @param <T> Provider 契约类型
     * @return 匹配的 Provider；没有匹配项时返回空 Optional
     * @throws com.innospots.nexus.base.exception.NexusException 选择存在歧义时抛出
     */
    <T extends CapabilityProvider> Optional<T> find(CapabilityType<T> type, Tags requiredTags);

    /**
     * 返回指定 Capability 类型已注册的全部活动 Provider。
     *
     * @param name Capability 逻辑名称
     * @param majorVersion API 主版本
     * @param <T> Provider 契约类型
     * @return 按注册顺序排列的不可变 Provider 列表
     */
    <T extends CapabilityProvider> List<T> findAll(String name, int majorVersion);

    /**
     * 返回指定 Capability 类型已注册的全部活动 Provider。
     *
     * @param type 包含 Java API 的 Capability 类型
     * @param <T> Provider 契约类型
     * @return 按注册顺序排列的不可变 Provider 列表
     */
    <T extends CapabilityProvider> List<T> findAll(CapabilityType<T> type);
}
