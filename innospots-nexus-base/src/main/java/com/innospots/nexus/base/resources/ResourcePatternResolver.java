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
 * Resolves resource location patterns (e.g. {@code classpath*:mapper/**\/*.xml})
 * into a list of {@link Resource} objects.
 * <p>
 * Supports the following location prefixes:
 * <ul>
 *   <li>{@code classpath*:} — scan all classpath roots for matching resources</li>
 *   <li>{@code classpath:} — scan only the first matching classpath root</li>
 *   <li>(no prefix) — same as {@code classpath*:}</li>
 * </ul>
 * Pattern matching uses Hutool's {@link AntPathMatcher} with the same syntax
 * as Spring's AntPathMatcher ({@code **}, {@code *}, {@code ?}).</pre>
 */
public class ResourcePatternResolver {

    private static final String CLASSPATH_ALL_PREFIX = "classpath*:";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String JAR_URL_SEPARATOR = "!/";

    private final ClassLoader classLoader;
    private final AntPathMatcher matcher;

    /** Creates a resolver using the default class loader. */
    public ResourcePatternResolver() {
        this(ClassUtil.getClassLoader());
    }

    /**
     * Creates a resolver with the specified class loader.
     *
     * @param classLoader the class loader used for classpath scanning
     */
    public ResourcePatternResolver(ClassLoader classLoader) {
        this.classLoader = classLoader != null ? classLoader : ClassUtil.getClassLoader();
        this.matcher = new AntPathMatcher();
    }

    /**
     * Returns a list of {@link Resource} objects matching the given location pattern.
     * <p>Each resource provides access to the underlying content via
     * {@link Resource#getStream()}, {@link Resource#readUtf8Str()}, etc.
     * The matched path information is discarded — use
     * {@link #getMatchedResources(String)} if the original match path is needed.</p>
     *
     * @param locationPattern the resource location pattern (e.g. {@code classpath*:mapper/**\/*.xml})
     * @return list of matching resources (never null)
     * @throws IOException if classpath scanning fails
     */
    public List<Resource> getResources(String locationPattern) throws IOException {
        return new ArrayList<>(getMatchedResources(locationPattern));
    }

    /**
     * Returns a list of {@link MatchedResource} objects matching the given
     * location pattern. Unlike {@link #getResources(String)}, each result
     * retains the original matched classpath path.
     *
     * @param locationPattern the resource location pattern
     * @return list of matched resources with path info (never null)
     * @throws IOException if classpath scanning fails
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
     * Matched resource that preserves the original classpath path in addition
     * to providing full {@link Resource} access via delegation.
     */
    public static class MatchedResource implements Resource {

        private final String path;
        private final Resource resource;

        MatchedResource(String path, Resource resource) {
            this.path = path;
            this.resource = resource;
        }

        /** Returns the classpath-relative matching path. */
        public String getPath() {
            return path;
        }

        /** Returns the underlying delegate resource. */
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

    // ---- Parse ----

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

    // ---- Exact match ----

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

    // ---- Scanning ----

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

    // ---- Deduplication ----

    private List<MatchedResource> distinctResources(List<MatchedResource> resources) {
        Map<String, MatchedResource> map = new LinkedHashMap<>();
        for (MatchedResource resource : resources) {
            URL url = resource.getUrl();
            String key = url == null ? resource.getPath() : url.toExternalForm();
            map.put(key, resource);
        }
        return new ArrayList<>(map.values());
    }

    // ---- Factory ----

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