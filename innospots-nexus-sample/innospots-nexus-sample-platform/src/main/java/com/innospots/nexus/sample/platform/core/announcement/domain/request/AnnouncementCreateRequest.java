package com.innospots.nexus.sample.platform.core.announcement.domain.request;

/**
 * 创建公告请求。
 *
 * @param title 标题
 * @param body 正文
 * @author Smars
 * @date 2026/09/26
 */
public record AnnouncementCreateRequest(String title, String body) {
}
