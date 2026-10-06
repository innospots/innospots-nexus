package com.innospots.nexus.core.setting.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity;
import com.innospots.nexus.core.setting.domain.enums.SettingScope;
import com.innospots.nexus.core.setting.domain.enums.SettingValueType;
import com.innospots.nexus.core.setting.operator.SystemSettingOperator;
import com.innospots.nexus.core.setting.status.SystemSettingStatusCode;
import com.innospots.nexus.core.setting.support.SettingKey;

/**
 * 运行时读取与更新持久化系统设置（按设置域、范围与 {@link SettingKey} 管理）。
 *
 * <p>首次读库无行时可引导插入；写入时校验长度、值类型与基础格式。事务边界由调用方（如 platform service）声明。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.operator.SystemSettingOperator
 * @see com.innospots.nexus.core.setting.status.SystemSettingStatusCode
 */
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingOperator settingOperator;

    /**
     * 读取字符串设置；无行时用部署默认值引导插入（类型固定为 {@link SettingValueType#STRING}）。
     *
     * @param domain             设置域
     * @param scope              作用范围
     * @param scopeId            范围 ID；GLOBAL 时可 null
     * @param settingKey         设置键
     * @param deploymentDefault  库内无行时的默认值
     * @return 当前持久化字符串
     * @throws NexusException 超长或库内类型非 STRING 时
     */
    public String getStringOrBootstrap(
            String domain,
            SettingScope scope,
            String scopeId,
            String settingKey,
            String deploymentDefault) {
        requireKeyParts(domain, settingKey, deploymentDefault);
        SystemSettingEntity entity = settingOperator.find(domain, scope, scopeId, settingKey);
        if (entity != null) {
            assertValueType(entity, SettingValueType.STRING);
            return entity.getSettingValue();
        }
        return settingOperator.insert(
                        domain,
                        scope,
                        scopeId,
                        settingKey,
                        SettingValueType.STRING,
                        deploymentDefault.trim())
                .getSettingValue();
    }

    /**
     * 读取类型化设置；无行时用 {@code deploymentDefault} 引导插入。
     *
     * @param settingKey        注册键（含值类型与解析器）
     * @param scopeId           范围 ID
     * @param deploymentDefault 缺省业务值
     * @param <T>               业务类型
     * @return 解析后的当前值
     * @throws NexusException 格式、类型或解析失败时
     */
    public <T> T getOrBootstrap(SettingKey<T> settingKey, String scopeId, T deploymentDefault) {
        Objects.requireNonNull(settingKey, "settingKey");
        Checks.notNull(deploymentDefault, "deploymentDefault");
        SystemSettingEntity entity = requireEntity(settingKey, scopeId, stringify(deploymentDefault), true);
        return parse(settingKey, entity.getSettingValue());
    }

    /**
     * 返回设置项 {@code updated_at}（必要时引导插入默认行）。
     *
     * @param settingKey          注册键
     * @param scopeId             范围 ID
     * @param deploymentDefaultRaw 引导插入用的原始字符串（与 {@link SettingKey#valueType()} 格式一致）
     * @return 最后更新时间
     */
    public LocalDateTime getUpdatedAtOrBootstrap(SettingKey<?> settingKey, String scopeId, String deploymentDefaultRaw) {
        Objects.requireNonNull(settingKey, "settingKey");
        SystemSettingEntity entity = requireEntity(settingKey, scopeId, deploymentDefaultRaw, true);
        return entity.getUpdatedAt();
    }

    /**
     * 写入字符串设置（{@link SettingValueType#STRING}）；值未变则跳过 UPDATE。
     *
     * @param domain      设置域
     * @param scope       作用范围
     * @param scopeId     范围 ID
     * @param settingKey  设置键
     * @param settingValue 新值
     * @return 持久化后的字符串
     */
    public String putString(
            String domain,
            SettingScope scope,
            String scopeId,
            String settingKey,
            String settingValue) {
        return putString(domain, scope, scopeId, settingKey, SettingValueType.STRING, settingValue);
    }

    /**
     * 写入字符串设置并显式声明值类型；值未变则跳过 UPDATE。
     *
     * @param domain       设置域
     * @param scope        作用范围
     * @param scopeId      范围 ID
     * @param settingKey   设置键
     * @param valueType    值类型（须与已存在行一致）
     * @param settingValue 新值
     * @return 持久化后的字符串
     * @throws NexusException 格式错误、类型不匹配或超长时
     */
    public String putString(
            String domain,
            SettingScope scope,
            String scopeId,
            String settingKey,
            SettingValueType valueType,
            String settingValue) {
        Objects.requireNonNull(valueType, "valueType");
        requireKeyParts(domain, settingKey, settingValue);
        validateValueFormat(valueType, settingValue.trim());
        String normalized = settingValue.trim();
        SystemSettingEntity entity = settingOperator.find(domain, scope, scopeId, settingKey);
        if (entity == null) {
            return settingOperator.insert(domain, scope, scopeId, settingKey, valueType, normalized)
                    .getSettingValue();
        }
        assertValueType(entity, valueType);
        if (normalized.equals(entity.getSettingValue())) {
            return entity.getSettingValue();
        }
        return settingOperator.updateValue(entity, normalized).getSettingValue();
    }

    /**
     * 写入类型化设置并返回解析结果。
     *
     * @param settingKey 注册键
     * @param scopeId    范围 ID
     * @param value      新业务值
     * @param <T>        业务类型
     * @return 写入后解析的值
     * @throws NexusException 格式、类型或解析失败时
     */
    public <T> T put(SettingKey<T> settingKey, String scopeId, T value) {
        Objects.requireNonNull(settingKey, "settingKey");
        Checks.notNull(value, "value");
        SystemSettingEntity entity = requireEntity(settingKey, scopeId, stringify(value), false);
        return parse(settingKey, entity.getSettingValue());
    }

    /**
     * 加载或引导插入设置行；{@code insertIfMissing=false} 时在值变化时更新库。
     *
     * @param insertIfMissing {@code true} 仅引导插入，不覆盖已有值（读路径）
     */
    private SystemSettingEntity requireEntity(
            SettingKey<?> settingKey,
            String scopeId,
            String rawValue,
            boolean insertIfMissing) {
        requireKeyParts(settingKey.domain(), settingKey.key(), rawValue);
        validateValueFormat(settingKey.valueType(), rawValue.trim());
        String normalized = rawValue.trim();
        SystemSettingEntity entity = settingOperator.find(
                settingKey.domain(), settingKey.scope(), scopeId, settingKey.key());
        if (entity == null) {
            return settingOperator.insert(
                    settingKey.domain(),
                    settingKey.scope(),
                    scopeId,
                    settingKey.key(),
                    settingKey.valueType(),
                    normalized);
        }
        assertValueType(entity, settingKey.valueType());
        // 读路径保留库内已有值；写路径在值变化时落库
        if (!insertIfMissing && !normalized.equals(entity.getSettingValue())) {
            return settingOperator.updateValue(entity, normalized);
        }
        return entity;
    }

    /**
     * 使用 {@link SettingKey#parser()} 解析库内字符串。
     *
     * @throws NexusException {@link SystemSettingStatusCode#SETTING_VALUE_INVALID}
     */
    private static <T> T parse(SettingKey<T> settingKey, String raw) {
        try {
            return settingKey.parser().apply(raw.trim());
        } catch (RuntimeException ex) {
            throw NexusException.build(SystemSettingStatusCode.SETTING_VALUE_INVALID);
        }
    }

    /** 防止同一键被不同模块以不同 {@link SettingValueType} 注册。 */
    private static void assertValueType(SystemSettingEntity entity, SettingValueType expected) {
        if (entity.valueTypeEnum() != expected) {
            throw NexusException.build(SystemSettingStatusCode.SETTING_VALUE_TYPE_MISMATCH);
        }
    }

    /**
     * 按 {@link SettingValueType} 做轻量格式校验（非 JSON Schema 级校验）。
     */
    private static void validateValueFormat(SettingValueType valueType, String normalized) {
        switch (valueType) {
            case BOOLEAN -> {
                if (!"true".equalsIgnoreCase(normalized) && !"false".equalsIgnoreCase(normalized)) {
                    throw NexusException.build(SystemSettingStatusCode.SETTING_VALUE_INVALID);
                }
            }
            case NUMBER -> {
                try {
                    new BigDecimal(normalized);
                } catch (NumberFormatException ex) {
                    throw NexusException.build(SystemSettingStatusCode.SETTING_VALUE_INVALID);
                }
            }
            case JSON -> {
                // 仅校验 JSON 文本前缀，完整语法由消费方或后续校验器负责
                if (!normalized.startsWith("{") && !normalized.startsWith("[")) {
                    throw NexusException.build(SystemSettingStatusCode.SETTING_VALUE_INVALID);
                }
            }
            case STRING, ENCRYPTED -> {
                // 长度已在 requireKeyParts 校验；ENCRYPTED 为 opaque 密文
            }
            default -> {
                // exhaustive
            }
        }
    }

    /** 枚举存 {@code name()}，其余类型用 {@code String.valueOf}。 */
    private static String stringify(Object value) {
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return String.valueOf(value);
    }

    /**
     * 域、键、值非空且值长度不超过 {@link SystemSettingEntity#SETTING_VALUE_MAX_LENGTH}。
     *
     * @throws NexusException {@link SystemSettingStatusCode#SETTING_VALUE_TOO_LONG}
     */
    private static void requireKeyParts(String domain, String settingKey, String settingValue) {
        Checks.notBlank(domain, "domain");
        Checks.notBlank(settingKey, "settingKey");
        Checks.notBlank(settingValue, "settingValue");
        String normalized = settingValue.trim();
        if (normalized.length() > SystemSettingEntity.SETTING_VALUE_MAX_LENGTH) {
            throw NexusException.build(SystemSettingStatusCode.SETTING_VALUE_TOO_LONG);
        }
    }
}
