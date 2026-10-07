package com.innospots.nexus.sample.platform.inbound.announcement.endpoint;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.sample.platform.core.announcement.domain.vo.AnnouncementVo;

/**
 * 对外公告只读 API。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Path("/platform/api/sample/announcements")
@Produces(MediaType.APPLICATION_JSON)
public interface InboundAnnouncementEndpoint {

    /**
     * 返回已发布公告列表。
     *
     * @return 公告列表
     */
    @GET
    R<List<AnnouncementVo>> listPublished();
}
