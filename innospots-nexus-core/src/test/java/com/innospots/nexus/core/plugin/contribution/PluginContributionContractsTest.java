package com.innospots.nexus.core.plugin.contribution;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 通用 Contribution 类型、注册表和事务句柄契约测试。 */
class PluginContributionContractsTest {

    private static final PluginContributionType<SampleContribution> TYPE =
            new PluginContributionType<>("fixture.contribution", 1);

    /** 验证类型标识和插件声明集合均保持不可变。 */
    @Test
    void keepsContributionTypeAndDefinitionImmutable() {
        SampleContribution contribution = new SampleContribution("sample");

        assertThat(contribution.type()).isEqualTo(TYPE);
        assertThat(List.of(contribution)).isUnmodifiable();
    }

    /** 验证同一 Contribution 类型不会被不同 Decoder 静默替换。 */
    @Test
    void rejectsDuplicateDecoderRegistration() {
        PluginContributionDecoder<SampleContribution> first = decoder();
        PluginContributionDecoder<SampleContribution> second = decoder();
        PluginContributionDecoderRegistry.Builder builder = PluginContributionDecoderRegistry.builder()
                .register(first);

        assertThatThrownBy(() -> builder.register(second))
                .isInstanceOfSatisfying(NexusException.class,
                        exception -> assertThat(exception.code())
                                .isEqualTo(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE.fullCode()));
    }

    /** 验证 prepared 句柄的 stage、commit、rollback 和 close 调用约束。 */
    @Test
    void enforcesPreparedHandleCommitOrderAndIdempotentCleanup() {
        boolean[] committed = {false};
        PreparedPluginContribution prepared = new PreparedPluginContribution() {
            private boolean staged;
            private boolean closed;

            @Override
            public void stage() {
                staged = true;
            }

            @Override
            public void commit() {
                if (!staged) {
                    throw NexusException.build(PluginStatusCode.RESOURCE_CONFLICT,
                            "fixture contribution must be staged first");
                }
                committed[0] = true;
            }

            @Override
            public void rollback() {
                committed[0] = false;
            }

            @Override
            public void close() {
                if (!closed) {
                    rollback();
                    closed = true;
                }
            }
        };

        assertThatThrownBy(prepared::commit)
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("staged");
        prepared.stage();
        prepared.commit();
        prepared.rollback();
        prepared.close();
        prepared.close();

        assertThat(committed[0]).isFalse();
    }

    private static PluginContributionDecoder<SampleContribution> decoder() {
        return new PluginContributionDecoder<>() {
            @Override
            public PluginContributionType<SampleContribution> type() {
                return TYPE;
            }

            @Override
            public SampleContribution decode(Map<String, Object> declaration) {
                return new SampleContribution(String.valueOf(declaration.get("value")));
            }
        };
    }

    private record SampleContribution(String value) implements PluginContribution {

        private SampleContribution {
            if (value == null || value.isBlank()) {
                throw NexusException.build(PluginStatusCode.DSL_STRUCTURE_INVALID,
                        "fixture contribution value is required");
            }
        }

        @Override
        public PluginContributionType<SampleContribution> type() {
            return TYPE;
        }
    }
}
