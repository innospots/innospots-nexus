package com.innospots.nexus.base.resources;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.UrlResource;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.text.AntPathMatcher;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 将资源位置模式（如 {@code classpath*:mapper/**\/*.xml}）解析为 {@link Resource} 列表。
 * <p>
 * 支持以下位置前缀：
 * <ul>
 *   <li>{@code classpath*:} — 扫描所有 classpath 根目录以匹配资源</li>
 *   <li>{@code classpath:} — 仅扫描第一个匹配的 classpath 根目录</li>
 *   <li>（无前缀）— 同 {@code classpath*:}</li>
 * </ul>
 * 模式匹配使用 Hutool 的 {@link AntPathMatcher}，语法与 Spring AntPathMatcher 相同
 * （{@code **}、{@code *}、{@code ?}）。
 *
 * @author Smars
 * @date 2026/09/13
 */
public class ResourcePatternResolver {

    private static final String CLASSPATH_ALL_PREFIX = "classpath*:";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String JAR_URL_SEPARATOR = "!/";

    private final ClassLoader classLoader;
    private final AntPathMatcher matcher;

    /**
     * 使用默认类加载器创建解析器。
     */
    public ResourcePatternResolver() {
        this(ClassUtil.getClassLoader());
    }

    /**
     * 使用指定类加载器创建解析器。
     *
     * @param classLoader 用于 classpath 扫描的类加载器
     */
    public ResourcePatternResolver(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : ClassUtil.getClassLoader();
        this.matcher = new AntPathMatcher();
    }

    /**
     * 返回匹配给定位置模式的 {@link Resource} 列表。
     * <p>每个资源可通过 {@link Resource#getStream()}、{@link Resource#readUtf8Str()} 等访问底层内容。
     * 匹配路径信息会被丢弃 — 若需要原始匹配路径，请使用 {@link #getMatchedResources(String)}。</p>
     *
     * @param locationPattern 资源位置模式（如 {@code classpath*:mapper/**\/*.xml}）
     * @return 匹配的资源列表（永不为 null）
     * @throws IOException classpath 扫描失败时
     */
    public List<Resource> getResources(String locationPattern) throws IOException {
        return new ArrayList<>(getMatchedResources(locationPattern));
    }

    /**
     * 返回匹配给定位置模式的 {@link MatchedResource} 列表。
     * 与 {@link #getResources(String)} 不同，每个结果保留原始匹配的 classpath 路径。
     *
     * @param locationPattern 资源位置模式
     * @return 带路径信息的匹配资源列表（永不为 null）
     * @throws IOException classpath 扫描失败时
     */
    public List<MatchedResource> getMatchedResources(String locationPattern) throws IOException {
        Location location = parseLocation(locationPattern);
        String pathPattern = normalizePath(location.path);

        if (!matcher.isPattern(pathPattern)) {
            return findExactResources(pathPattern, location.scanAll);
        }

        String rootDir = determineRootDir(pathPattern);
        List<MatchedResource> result = new ArrayList<>();

        if (location.scanAll) {
            Enumeration<URL> urls = classLoader.getResources(rootDir);
            while (urls.hasMoreElements()) {
                scanRootUrl(urls.nextElement(), rootDir, pathPattern, result);
            }
        } else {
            URL url = classLoader.getResource(rootDir);
            if (url != null) {
                scanRootUrl(url, rootDir, pathPattern, result);
            }
        }

        return distinctResources(result);
    }

    /**
     * 匹配资源，在提供完整 {@link Resource} 委托访问的同时保留原始 classpath 路径。
     *
     * @author Smars
     * @date 2026/09/13
     */
    public static class MatchedResource implements Resource {

        private final String path;
        private final Resource resource;

        MatchedResource(String path, Resource resource) {
            this.path = path;
            this.resource = resource;
        }

        /**
         * 返回 classpath 相对匹配路径。
         *
         * @return 匹配路径
         */
        public String getPath() {
            return path;
        }

        /**
         * 返回底层委托资源。
         *
         * @return 委托资源
         */
        public Resource getResource() {
            return resource;
        }

        @Override
        public String getName() {
            return resource.getName();
        }

        @Override
        public URL getUrl() {
            return resource.getUrl();
        }

        @Override
        public InputStream getStream() {
            return resource.getStream();
        }

        @Override
        public boolean isModified() {
            return resource.isModified();
        }

        @Override
        public String toString() {
            return "MatchedResource{path='" + path + "', resource=" + resource + '}';
        }
    }

    private record Location(boolean scanAll, String path) {
    }

    private Location parseLocation(String locationPattern) {
        if (locationPattern == null || locationPattern.isBlank()) {
            throw new IllegalArgumentException("Location pattern must not be blank");
        }
        String path = locationPattern.trim();
        if (path.startsWith(CLASSPATH_ALL_PREFIX)) {
            return new Location(true, path.substring(CLASSPATH_ALL_PREFIX.length()));
        }
        if (path.startsWith(CLASSPATH_PREFIX)) {
            return new Location(false, path.substring(CLASSPATH_PREFIX.length()));
        }
        return new Location(true, path);
    }

    private String normalizePath(String path) {
        String result = path;
        if (result.startsWith("/")) {
            result = result.substring(1);
        }
        return result;
    }

    private String determineRootDir(String pathPattern) {
        int wildcardIndex = findWildcardIndex(pathPattern);
        if (wildcardIndex == -1) {
            return pathPattern;
        }
        int lastSlash = pathPattern.lastIndexOf('/', wildcardIndex);
        return lastSlash == -1 ? "" : pathPattern.substring(0, lastSlash);
    }

    private int findWildcardIndex(String path) {
        int asterisk = path.indexOf('*');
        int question = path.indexOf('?');
        if (asterisk == -1 && question == -1) {
            return -1;
        }
        if (asterisk == -1) {
            return question;
        }
        if (question == -1) {
            return asterisk;
        }
        return Math.min(asterisk, question);
    }

    private List<MatchedResource> findExactResources(String path, boolean scanAll) throws IOException {
        List<MatchedResource> result = new ArrayList<>();
        if (scanAll) {
            Enumeration<URL> urls = classLoader.getResources(path);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                result.add(createMatchedResource(path, url));
            }
        } else {
            URL url = classLoader.getResource(path);
            if (url != null) {
                result.add(createMatchedResource(path, url));
            }
        }
        return distinctResources(result);
    }

    private void scanRootUrl(URL rootUrl, String rootDir, String pathPattern, List<MatchedResource> result) {
        String protocol = rootUrl.getProtocol();
        if ("file".equals(protocol)) {
            scanFileSystem(rootUrl, rootDir, pathPattern, result);
        } else if ("jar".equals(protocol)) {
            scanJar(rootUrl, rootDir, pathPattern, result);
        }
    }

    private void scanFileSystem(URL rootUrl, String rootDir, String pathPattern, List<MatchedResource> result) {
        try {
            File rootFile = new File(rootUrl.toURI());
            if (!rootFile.exists() || !rootFile.isDirectory()) {
                return;
            }
            scanFileTree(rootFile, rootFile, rootDir, pathPattern, result);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve root URL to file: " + rootUrl, e);
        }
    }

    private void scanFileTree(File rootFile, File currentFile, String rootDir, String pathPattern,
                               List<MatchedResource> result) {
        File[] files = currentFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            String relativePath = buildRelativePath(rootFile, file);
            if (file.isDirectory()) {
                scanFileTree(rootFile, file, rootDir, pathPattern, result);
            } else {
                String resourcePath = rootDir.isEmpty()
                        ? relativePath
                        : rootDir + "/" + relativePath;
                if (matcher.match(pathPattern, resourcePath)) {
                    try {
                        result.add(createMatchedResource(resourcePath, file.toURI().toURL()));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create URL for file: " + file, e);
                    }
                }
            }
        }
    }

    private String buildRelativePath(File root, File file) {
        String rootPath = root.getAbsolutePath().replace('\\', '/');
        String filePath = file.getAbsolutePath().replace('\\', '/');
        String relative = filePath.substring(rootPath.length());
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        return relative;
    }

    private void scanJar(URL rootUrl, String rootDir, String pathPattern, List<MatchedResource> result) {
        String jarFileUrl = extractJarFileUrl(rootUrl);
        File jarFile = new File(jarFileUrl);
        if (!jarFile.exists()) {
            return;
        }
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName();
                if (rootDir.isEmpty() || entryName.startsWith(rootDir)) {
                    if (matcher.match(pathPattern, entryName)) {
                        URL matchedUrl = new URL("jar:" + rootUrl.toExternalForm() + "!/" + entryName);
                        result.add(createMatchedResource(entryName, matchedUrl));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan jar file: " + jarFileUrl, e);
        }
    }

    private String extractJarFileUrl(URL jarUrl) {
        String urlString = jarUrl.toExternalForm();
        int separatorIndex = urlString.indexOf(JAR_URL_SEPARATOR);
        if (separatorIndex != -1) {
            urlString = urlString.substring(0, separatorIndex);
        }
        if (urlString.startsWith("jar:")) {
            urlString = urlString.substring(4);
        }
        if (urlString.startsWith("file:")) {
            urlString = urlString.substring(5);
        }
        return urlString;
    }

    // 按 URL 去重，避免多 classpath 根目录扫描时重复返回同一资源
    private List<MatchedResource> distinctResources(List<MatchedResource> resources) {
        Map<String, MatchedResource> map = new LinkedHashMap<>();
        for (MatchedResource resource : resources) {
            URL url = resource.getUrl();
            String key = url == null ? resource.getPath() : url.toExternalForm();
            map.put(key, resource);
        }
        return new ArrayList<>(map.values());
    }

    private MatchedResource createMatchedResource(String path, URL url) {
        String normalizedPath = normalizePath(path);
        String name = extractFileName(normalizedPath);
        Resource delegate = new UrlResource(url, name);
        return new MatchedResource(normalizedPath, delegate);
    }

    private static String extractFileName(String path) {
        if (StrUtil.isBlank(path)) {
            return "";
        }
        int index = path.lastIndexOf('/');
        return index < 0 ? path : path.substring(index + 1);
    }
}
