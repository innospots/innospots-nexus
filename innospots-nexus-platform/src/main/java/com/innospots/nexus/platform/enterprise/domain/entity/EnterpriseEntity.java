package com.innospots.nexus.platform.enterprise.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.BaseEntity;

/**
 * 运维侧法定企业档案，与 {@code nx_tenant} 一对一。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@Entity
@Table(name = EnterpriseEntity.TABLE_NAME, indexes = {
        @Index(name = "uk_nx_enterprise_tenant", columnList = "tenant_id", unique = true)
})
@TableName(EnterpriseEntity.TABLE_NAME)
public class EnterpriseEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_enterprise";

    /**
     * 企业标识符。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String enterpriseId;
    /**
     * 返回主键前缀。
     * @return 操作结果
     */

    
    @Override
    public String idPrefix() {
        return "ent";
    }

    /**
     * 该档案所属的租户。
     */
    @Column(length = 32, nullable = false)
    private String tenantId;

    /**
     * 法定注册名称。
     */
    @Column(length = 256, nullable = false)
    private String legalName;

    /**
     * 统一社会信用代码或同等标识符。
     */
    @Column(length = 64)
    private String creditCode;

    /**
     * 行业分类。
     */
    @Column(length = 64)
    private String industry;

    /**
     * 主要联系人姓名。
     */
    @Column(length = 128)
    private String contactName;

    /**
     * 主要联系人电话。
     */
    @Column(length = 32)
    private String contactPhone;

    /**
     * 主要联系人邮箱。
     */
    @Column(length = 128)
    private String contactEmail;

    /**
     * 注册或通讯地址。
     */
    @Column(length = 512)
    private String address;

    /**
     * 可扩展 JSON 或自由格式属性。
     */
    @Column(length = 1024)
    private String extra;
}
