package com.innospots.nexus.sample.platform.core.announcement.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.sample.platform.core.announcement.domain.enums.AnnouncementStatus;

/**
 * 运营平台公告持久化记录。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Getter
@Setter
@Entity
@Table(name = AnnouncementEntity.TABLE_NAME)
@TableName(AnnouncementEntity.TABLE_NAME)
public class AnnouncementEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_sample_announcement";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String announcementId;

    @Column(length = 128, nullable = false)
    private String title;

    @Column(length = 4000, nullable = false)
    private String body;

    @Column(length = 16, nullable = false)
    private AnnouncementStatus status;
}
