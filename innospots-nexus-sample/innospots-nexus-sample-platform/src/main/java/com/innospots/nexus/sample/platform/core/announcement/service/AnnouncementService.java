package com.innospots.nexus.sample.platform.core.announcement.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.sample.platform.core.announcement.domain.request.AnnouncementCreateRequest;
import com.innospots.nexus.sample.platform.core.announcement.domain.vo.AnnouncementVo;
import com.innospots.nexus.sample.platform.core.announcement.operator.AnnouncementOperator;

/**
 * 公告领域编排（console / inbound 共享）。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementOperator announcementOperator;

    /**
     * 创建草稿并可选立即发布。
     *
     * @param request 创建数据
     * @param publish 是否在创建后发布
     * @return 公告概要
     */
    public AnnouncementVo create(AnnouncementCreateRequest request, boolean publish) {
        var entity = announcementOperator.insertDraft(request.title(), request.body());
        if (publish) {
            entity = announcementOperator.markPublished(entity.getAnnouncementId());
        }
        return new AnnouncementVo(entity.getAnnouncementId(), entity.getTitle(), entity.getStatus());
    }
}
