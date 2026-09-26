package com.innospots.nexus.sample.platform.core.announcement.domain.enums;

/**
 * 平台公告生命周期状态。
 *
 * @author Smars
 * @date 2026/09/26
 */
public enum AnnouncementStatus {

    /**
     * 草稿，仅管理台可见。
     */
    DRAFT,

    /**
     * 已发布，对外可读。
     */
    PUBLISHED,

    /**
     * 已归档，对外不可见。
     */
    ARCHIVED
}
