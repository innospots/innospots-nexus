package com.innospots.nexus.spring.service.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innospots.nexus.service.contract.policy.ResponseProfile;

/**
 * Spring {@code service.*} 绑定属性。
 */
@ConfigurationProperties(prefix = "service")
public class ServiceProperties {

    private boolean enabled = true;
    private String name = "nexus-service";
    private ResponseProfile responseProfile = ResponseProfile.LEGACY;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResponseProfile getResponseProfile() {
        return responseProfile;
    }

    public void setResponseProfile(ResponseProfile responseProfile) {
        this.responseProfile = responseProfile;
    }
}
