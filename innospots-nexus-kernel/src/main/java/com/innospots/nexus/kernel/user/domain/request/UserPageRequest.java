package com.innospots.nexus.kernel.user.domain.request;

import com.innospots.nexus.base.domain.request.BasePageRequest;

/**
 * Paginated user query request.
 */
public class UserPageRequest extends BasePageRequest {

    private String userName;
    private String realName;
    private String email;
    private String mobile;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
