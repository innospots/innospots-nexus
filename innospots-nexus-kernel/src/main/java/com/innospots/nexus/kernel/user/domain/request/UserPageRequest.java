package com.innospots.nexus.kernel.user.domain.request;

import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.base.domain.request.BasePageRequest;

/**
 * Paginated user query request.
 */
@Getter
@Setter
public class UserPageRequest extends BasePageRequest {

    private String userName;
    private String realName;
    private String email;
    private String mobile;

}
