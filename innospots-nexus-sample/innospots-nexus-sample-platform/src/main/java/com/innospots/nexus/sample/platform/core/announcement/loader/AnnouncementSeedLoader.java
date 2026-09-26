package com.innospots.nexus.sample.platform.core.announcement.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.sample.platform.core.announcement.domain.request.AnnouncementCreateRequest;
import com.innospots.nexus.sample.platform.core.announcement.service.AnnouncementService;

/**
 * 启动期种子公告加载器示例。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Slf4j
@RequiredArgsConstructor
public class AnnouncementSeedLoader {

    private final AnnouncementService announcementService;

    /**
     * 在应用启动时写入示例公告（幂等由调用方保证）。
     */
    public void loadDefaults() {
        announcementService.create(
                new AnnouncementCreateRequest("Welcome", "Sample platform extension is active."),
                true);
        log.info("sample announcement seed loaded");
    }
}
