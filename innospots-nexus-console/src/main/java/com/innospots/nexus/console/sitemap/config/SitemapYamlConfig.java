package com.innospots.nexus.console.sitemap.config;

/**
 * Classpath sitemap YAML 资源定位配置。
 *
 * <p>默认路径：{@code ui-pages/{domainKey}/sitemap.yaml}。</p>
 */
public final class SitemapYamlConfig {

    private final String resourceRoot;
    private final String fileName;

    /**
     * 使用默认根目录 {@code ui-pages} 与文件名 {@code sitemap.yaml}。
     */
    public SitemapYamlConfig() {
        this("ui-pages", "sitemap.yaml");
    }

    /**
     * @param resourceRoot classpath 根段（如 {@code ui-pages})
     * @param fileName     sitemap 文件名
     */
    public SitemapYamlConfig(String resourceRoot, String fileName) {
        if (resourceRoot == null || resourceRoot.isBlank()) {
            throw new IllegalArgumentException("resourceRoot is required");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName is required");
        }
        this.resourceRoot = resourceRoot.trim();
        this.fileName = fileName.trim();
    }

    /**
     * 默认配置实例。
     */
    public static SitemapYamlConfig defaults() {
        return new SitemapYamlConfig();
    }

    /**
     * 解析 classpath 资源路径。
     *
     * @param domainKey 领域键（{@code ui-pages} 首段目录名）
     * @return 不含前导 {@code /} 的资源路径
     */
    public String resourcePath(String domainKey) {
        if (domainKey == null || domainKey.isBlank()) {
            throw new IllegalArgumentException("domainKey is required");
        }
        return resourceRoot + "/" + domainKey.trim() + "/" + fileName;
    }
}
