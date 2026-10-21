package com.innospots.nexus.console.sitemap.config;

/**
 * Classpath meta-sitemap YAML 资源定位。
 *
 * <p>默认：{@code ui-pages/meta-sitemap.yaml}（与前端工程 {@code sitemap.yaml} 无关）。</p>
 */
public final class SitemapYamlConfig {

    private final String resourcePath;

    private SitemapYamlConfig(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("resourcePath is required");
        }
        this.resourcePath = resourcePath.trim();
    }

    /**
     * 默认 meta-sitemap 路径。
     */
    public static SitemapYamlConfig metaSitemapDefaults() {
        return ofResourcePath("ui-pages/meta-sitemap.yaml");
    }

    /**
     * 指定 classpath 资源路径（测试或定制部署用）。
     */
    public static SitemapYamlConfig ofResourcePath(String resourcePath) {
        return new SitemapYamlConfig(resourcePath);
    }

    /**
     * @return 不含前导 {@code /} 的 classpath 资源路径
     */
    public String resourcePath() {
        return resourcePath;
    }
}
