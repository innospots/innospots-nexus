package com.innospots.nexus.sample.platform.core.announcement.domain.vo;

import com.innospots.nexus.sample.platform.core.announcement.domain.enums.AnnouncementStatus;

/**
 * 公告概要。
 *
 * @param announcementId 标识
 * @param title 标题
 * @param status 状态
 * @author Smars
 * @date 2026/09/26
 */
public record AnnouncementVo(String announcementId, String title, AnnouncementStatus status) {
}
