package com.innospots.nexus.sample.platform.console.announcement.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.sample.platform.core.announcement.domain.request.AnnouncementCreateRequest;
import com.innospots.nexus.sample.platform.core.announcement.domain.vo.AnnouncementVo;

/**
 * 管理台公告 Jakarta REST 契约。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Path("/platform/console/sample/announcements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ConsoleAnnouncementEndpoint {

    /**
     * 创建并发布公告。
     *
     * @param request 创建数据
     * @return 公告概要
     */
    @POST
    @Path("/publish")
    R<AnnouncementVo> publish(AnnouncementCreateRequest request);
}
