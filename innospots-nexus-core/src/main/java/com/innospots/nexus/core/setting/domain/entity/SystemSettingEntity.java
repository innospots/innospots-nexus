package com.innospots.nexus.core.setting.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.core.setting.domain.enums.SettingScope;
import com.innospots.nexus.core.setting.domain.enums.SettingValueType;

/**
 * 持久化系统设置项（{@code nx_system_setting}）。
 *
 * <p>由 {@code setting_domain + setting_scope + scope_id + setting_key} 唯一标识一条配置；
 * {@code value_type} 在插入后不变，仅 {@code setting_value} 可更新。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.service.SystemSettingService
 * @see com.innospots.nexus.core.setting.support.SettingKey
 */
@Getter
@Setter
@Entity
@Table(
        name = SystemSettingEntity.TABLE_NAME,
        uniqueConstraints = @UniqueConstraint(
                name = "uk_nx_system_setting_scope_key",
                columnNames = {"setting_domain", "setting_scope", "scope_id", "setting_key"}),
        indexes = @Index(name = "idx_nx_system_setting_domain", columnList = "setting_domain"))
@TableName(SystemSettingEntity.TABLE_NAME)
public class SystemSettingEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_system_setting";

    /** {@link #settingValue} 最大字符数（VARCHAR 阶梯扩展档 2048）。 */
    public static final int SETTING_VALUE_MAX_LENGTH = 2048;

    /** 全局范围时 {@code scopeId} 使用空串，便于 MySQL 唯一索引 treat NULL 差异。 */
    public static final String GLOBAL_SCOPE_ID = "";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String settingId;

    @Override
    public String idPrefix() {
        return "set";
    }

    /** 设置域，由上层模块约定（如 {@code platform}）。 */
    @Column(length = 64, nullable = false)
    private String settingDomain;

    /** {@link SettingScope#name()}。 */
    @Column(length = 32, nullable = false)
    private String settingScope;

    /** 范围实例 ID；{@link SettingScope#GLOBAL} 时为 {@link #GLOBAL_SCOPE_ID}。 */
    @Column(length = 32, nullable = false)
    private String scopeId;

    /** 域内设置键。 */
    @Column(length = 128, nullable = false)
    private String settingKey;

    /** {@link SettingValueType#name()}。 */
    @Column(name = "value_type", length = 32, nullable = false)
    private String valueType;

    /** 序列化后的设置值（长度受 {@link #SETTING_VALUE_MAX_LENGTH} 约束）。 */
    @Column(length = SETTING_VALUE_MAX_LENGTH, nullable = false)
    private String settingValue;

    /**
     * @return 作用范围枚举
     */
    public SettingScope settingScopeEnum() {
        return SettingScope.valueOf(settingScope);
    }

    /**
     * @return 值类型枚举
     */
    public SettingValueType valueTypeEnum() {
        return SettingValueType.valueOf(valueType);
    }
}
