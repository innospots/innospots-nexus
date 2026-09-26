package com.innospots.nexus.sample.platform.inbound.announcement.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.sample.platform.core.announcement.dao.AnnouncementDao;
import com.innospots.nexus.sample.platform.core.announcement.domain.entity.AnnouncementEntity;
import com.innospots.nexus.sample.platform.core.announcement.domain.enums.AnnouncementStatus;
import com.innospots.nexus.sample.platform.core.announcement.domain.vo.AnnouncementVo;

/**
 * 对外公告只读查询。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class InboundAnnouncementQueryService {

    private final AnnouncementDao announcementDao;

    /**
     * 列出已发布公告。
     *
     * @return 公告概要列表
     */
    public List<AnnouncementVo> listPublished() {
        return announcementDao.selectList(
                        Wrappers.<AnnouncementEntity>lambdaQuery()
                                .eq(AnnouncementEntity::getStatus, AnnouncementStatus.PUBLISHED))
                .stream()
                .map(entity -> new AnnouncementVo(
                        entity.getAnnouncementId(), entity.getTitle(), entity.getStatus()))
                .toList();
    }
}
