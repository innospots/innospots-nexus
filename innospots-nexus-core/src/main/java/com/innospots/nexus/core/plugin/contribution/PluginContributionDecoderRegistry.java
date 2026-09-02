package com.innospots.nexus.core.plugin.contribution;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 宿主显式注册的通用 Contribution Decoder 表。
 *
 * <p>构建后不可变，可在多个编译器或运行时实例间安全共享。</p>
 */
public final class PluginContributionDecoderRegistry {

    private final Map<PluginContributionType<?>, PluginContributionDecoder<?>> decoders;

    private PluginContributionDecoderRegistry(Map<PluginContributionType<?>, PluginContributionDecoder<?>> decoders) {
        this.decoders = Map.copyOf(decoders);
    }

    /**
     * 创建空注册表构建器。
     *
     * @return 新的可变构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 按类型查询 Decoder。
     *
     * @param type Contribution 类型标识
     * @return 已注册 Decoder；未注册时返回空 Optional
     */
    public Optional<PluginContributionDecoder<?>> find(PluginContributionType<?> type) {
        return Optional.ofNullable(decoders.get(type));
    }

    /**
     * 返回不可变 Decoder 快照。
     *
     * @return 类型到 Decoder 的不可变映射
     */
    public Map<PluginContributionType<?>, PluginContributionDecoder<?>> snapshot() {
        return decoders;
    }

    /** Decoder 注册构建器。 */
    public static final class Builder {

        private final Map<PluginContributionType<?>, PluginContributionDecoder<?>> decoders = new LinkedHashMap<>();

        /**
         * 注册一个 Decoder，重复类型只能使用同一实例。
         *
         * @param decoder 要注册的 Decoder
         * @return 当前构建器
         * @throws NexusException Decoder 或其类型为空，或同一类型注册了不同实例时抛出
         */
        public <T extends PluginContribution> Builder register(PluginContributionDecoder<T> decoder) {
            if (decoder == null || decoder.type() == null) {
                throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                        "contribution decoder is required");
            }
            PluginContributionDecoder<?> previous = decoders.putIfAbsent(decoder.type(), decoder);
            if (previous != null && previous != decoder) {
                throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                        "duplicate contribution decoder: " + decoder.type());
            }
            return this;
        }

        /**
         * 构建不可变注册表。
         *
         * @return 冻结后的注册表快照
         */
        public PluginContributionDecoderRegistry build() {
            return new PluginContributionDecoderRegistry(new LinkedHashMap<>(decoders));
        }
    }
}
