package com.innospots.nexus.sample.platform.core.announcement.operator;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.sample.platform.core.announcement.dao.AnnouncementDao;
import com.innospots.nexus.sample.platform.core.announcement.domain.entity.AnnouncementEntity;
import com.innospots.nexus.sample.platform.core.announcement.domain.enums.AnnouncementStatus;

/**
 * 公告单表持久化操作。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class AnnouncementOperator {

    private final AnnouncementDao announcementDao;

    /**
     * 插入草稿公告。
     *
     * @param title 标题
     * @param body 正文
     * @return persisted 实体
     */
    public AnnouncementEntity insertDraft(String title, String body) {
        requireText(title, "title");
        requireText(body, "body");
        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setTitle(title);
        entity.setBody(body);
        entity.setStatus(AnnouncementStatus.DRAFT);
        announcementDao.insert(entity);
        return entity;
    }

    /**
     * 将公告标记为已发布。
     *
     * @param announcementId 标识
     * @return updated 实体
     */
    public AnnouncementEntity markPublished(String announcementId) {
        AnnouncementEntity entity = announcementDao.selectById(announcementId);
        if (entity == null) {
            throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND, "announcement not found");
        }
        entity.setStatus(AnnouncementStatus.PUBLISHED);
        announcementDao.updateById(entity);
        return entity;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, field + " is required");
        }
    }
}
