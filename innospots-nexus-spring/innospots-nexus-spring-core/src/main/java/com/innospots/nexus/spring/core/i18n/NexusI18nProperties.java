package com.innospots.nexus.spring.core.i18n;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Nexus Spring i18n 绑定属性。
 * <p>
 * 默认与 Spring Boot {@code spring.messages.*} 对齐；当容器内尚无 {@code MessageSource} Bean 时，
 * 按 {@link #basenames()} 创建 {@link org.springframework.context.support.ResourceBundleMessageSource}。
 *
 * @author Smars
 * @date 2026/09/16
 */
@ConfigurationProperties(prefix = "nexus.i18n")
public class NexusI18nProperties {

    /**
     * 是否启用 Nexus i18n 桥接（注册 {@link com.innospots.nexus.base.i18n.I18nMessageResolver}）。
     */
    private boolean enabled = true;

    /**
     * 是否在 Web 请求中将 {@link org.springframework.context.i18n.LocaleContextHolder}
     * 同步到 {@link com.innospots.nexus.base.i18n.I18nConverter}。
     */
    private boolean syncWebLocale = true;

    /**
     * ResourceBundle 基名列表（无 {@code MessageSource} Bean 时使用）。
     */
    private List<String> basenames = new ArrayList<>(List.of("messages"));

    /**
     * ResourceBundle 编码。
     */
    private String encoding = "UTF-8";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSyncWebLocale() {
        return syncWebLocale;
    }

    public void setSyncWebLocale(boolean syncWebLocale) {
        this.syncWebLocale = syncWebLocale;
    }

    public List<String> basenames() {
        return List.copyOf(basenames);
    }

    public List<String> getBasenames() {
        return basenames;
    }

    public void setBasenames(List<String> basenames) {
        this.basenames = basenames == null ? new ArrayList<>() : new ArrayList<>(basenames);
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
