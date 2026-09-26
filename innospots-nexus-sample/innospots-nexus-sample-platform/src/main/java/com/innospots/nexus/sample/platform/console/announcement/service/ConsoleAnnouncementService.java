package com.innospots.nexus.sample.platform.console.announcement.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.sample.platform.core.announcement.domain.request.AnnouncementCreateRequest;
import com.innospots.nexus.sample.platform.core.announcement.domain.vo.AnnouncementVo;
import com.innospots.nexus.sample.platform.core.announcement.service.AnnouncementService;

/**
 * 管理台侧公告编排（发布、草稿管理等）。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class ConsoleAnnouncementService {

    private final AnnouncementService announcementService;

    /**
     * 管理台创建并立即发布公告。
     *
     * @param request 创建数据
     * @return 公告概要
     */
    public AnnouncementVo publish(AnnouncementCreateRequest request) {
        return announcementService.create(request, true);
    }
}
