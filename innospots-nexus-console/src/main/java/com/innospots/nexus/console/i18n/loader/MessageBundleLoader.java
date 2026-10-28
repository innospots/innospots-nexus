package com.innospots.nexus.console.i18n.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.i18n.domain.vo.MessageBundleVo;
import com.innospots.nexus.console.i18n.status.I18nStatusCode;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 从 classpath 读取 Spring MessageResource 风格的语言包文件，并按 locale
 * 与 namespace 缓存不可变快照。
 *
 * <p>文件使用 UTF-8 编码；基础 {@code messages.properties} 表示
 * {@code zh-CN}，英语资源为 {@code messages_en.properties}。</p>
 *
 * @author Smars
 * @date 2026/10/26
 */
public final class MessageBundleLoader {

    private static final String RESOURCE_ROOT = "i18n/console/";
    private static final String NAMESPACE_INDEX = RESOURCE_ROOT + "namespaces";
    private static final String RESOURCE_FILE = "messages";
    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9][a-z0-9-]{0,63}");

    private final ClassLoader classLoader;
    private final ConcurrentMap<CacheKey, MessageBundleVo> cache = new ConcurrentHashMap<>();
    private volatile List<String> namespaces;

    /**
     * 使用当前线程上下文类加载器创建加载器。
     */
    public MessageBundleLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 使用指定类加载器创建加载器。
     *
     * @param classLoader 语言包资源类加载器
     */
    public MessageBundleLoader(ClassLoader classLoader) {
        this.classLoader = classLoader == null
                ? MessageBundleLoader.class.getClassLoader()
                : classLoader;
    }

    /**
     * 返回 classpath 语言包索引声明的 namespace。
     *
     * @return 按索引顺序排列的不可变 namespace 列表
     */
    public List<String> namespaces() {
        List<String> cached = namespaces;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (namespaces == null) {
                namespaces = List.copyOf(readNamespaces());
            }
            return namespaces;
        }
    }

    /**
     * 加载指定 locale 与 namespace 的语言包。
     *
     * @param locale 规范语言代码
     * @param namespace 语言包命名空间
     * @return 不可变语言包快照
     */
    public MessageBundleVo load(String locale, String namespace) {
        CacheKey cacheKey = new CacheKey(locale, requireValidNamespace(namespace));
        return cache.computeIfAbsent(cacheKey, this::readBundle);
    }

    /**
     * 加载索引声明的全部 namespace。
     *
     * @param locale 规范语言代码
     * @return 按索引顺序排列的不可变语言包映射
     */
    public Map<String, MessageBundleVo> loadAll(String locale) {
        Map<String, MessageBundleVo> bundles = new LinkedHashMap<>();
        for (String namespace : namespaces()) {
            bundles.put(namespace, load(locale, namespace));
        }
        return Collections.unmodifiableMap(bundles);
    }

    private MessageBundleVo readBundle(CacheKey key) {
        Map<String, String> messages = readMessages(key.locale(), key.namespace());
        if (messages.isEmpty()) {
            throw ConsoleExceptions.build(I18nStatusCode.BUNDLE_NOT_FOUND);
        }
        StringBuilder content = new StringBuilder();
        messages.forEach((messageKey, message) -> content
                .append(messageKey)
                .append('=')
                .append(message)
                .append('\n'));
        return new MessageBundleVo(CryptoUtils.sha256Hex(content.toString()), messages);
    }

    private Map<String, String> readMessages(String locale, String namespace) {
        String keyPrefix = namespace + ".";
        Map<String, String> sorted = new TreeMap<>();
        String baseResource = RESOURCE_ROOT + namespace + "/"
                + RESOURCE_FILE + PROPERTIES_SUFFIX;
        readResource(baseResource, keyPrefix, sorted);
        for (String candidate : localizedResources(locale)) {
            String resource = RESOURCE_ROOT + namespace + "/" + candidate;
            readResource(resource, keyPrefix, sorted);
        }
        if (!sorted.isEmpty()) {
            return immutableMessages(sorted);
        }
        throw ConsoleExceptions.build(I18nStatusCode.BUNDLE_NOT_FOUND);
    }

    private List<String> readNamespaces() {
        Set<String> discovered = new LinkedHashSet<>();
        try (InputStream input = classLoader.getResourceAsStream(NAMESPACE_INDEX)) {
            if (input == null) {
                throw ConsoleExceptions.build(I18nStatusCode.BUNDLE_NOT_FOUND);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String normalized = line.trim();
                    if (normalized.isEmpty() || normalized.startsWith("#")) {
                        continue;
                    }
                    String namespace = requireValidNamespace(normalized);
                    if (!discovered.add(namespace)) {
                        throw ConsoleExceptions.build(
                                I18nStatusCode.BUNDLE_LOAD_FAILED,
                                "Duplicate message namespace: " + namespace);
                    }
                }
            }
        } catch (IOException error) {
            throw ConsoleExceptions.build(I18nStatusCode.BUNDLE_LOAD_FAILED, error);
        }
        if (discovered.isEmpty()) {
            throw ConsoleExceptions.build(
                    I18nStatusCode.BUNDLE_LOAD_FAILED,
                    "Message namespace index must not be empty");
        }
        return List.copyOf(discovered);
    }

    private static String requireValidNamespace(String namespace) {
        String normalized = namespace == null ? "" : namespace.trim();
        if (!NAMESPACE_PATTERN.matcher(normalized).matches()) {
            throw ConsoleExceptions.build(
                    I18nStatusCode.BUNDLE_LOAD_FAILED,
                    "Invalid message namespace: " + namespace);
        }
        return normalized;
    }

    private void readResource(String resource, String keyPrefix, Map<String, String> messages) {
        try (InputStream input = classLoader.getResourceAsStream(resource)) {
            if (input == null) {
                return;
            }
            Map<String, String> resourceMessages = new LinkedHashMap<>();
            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                ResourceBundle bundle = new PropertyResourceBundle(reader);
                for (String key : bundle.keySet()) {
                    String normalizedKey = key.trim();
                    if (!normalizedKey.startsWith(keyPrefix)) {
                        throw ConsoleExceptions.build(
                                I18nStatusCode.BUNDLE_LOAD_FAILED,
                                "Message key must use its namespace prefix: " + normalizedKey);
                    }
                    String previous = resourceMessages.putIfAbsent(normalizedKey, bundle.getString(key));
                    if (previous != null) {
                        throw ConsoleExceptions.build(
                                I18nStatusCode.BUNDLE_LOAD_FAILED,
                                "Duplicate message key: " + normalizedKey);
                    }
                }
            }
            messages.putAll(resourceMessages);
        } catch (IOException | IllegalArgumentException error) {
            throw ConsoleExceptions.build(I18nStatusCode.BUNDLE_LOAD_FAILED, error);
        }
    }

    private static List<String> localizedResources(String locale) {
        List<String> candidates = new ArrayList<>();
        if ("en-US".equals(locale)) {
            candidates.add(RESOURCE_FILE + "_en" + PROPERTIES_SUFFIX);
            candidates.add(RESOURCE_FILE + "_en_US" + PROPERTIES_SUFFIX);
        } else if ("zh-CN".equals(locale)) {
            candidates.add(RESOURCE_FILE + "_zh" + PROPERTIES_SUFFIX);
            candidates.add(RESOURCE_FILE + "_zh_CN" + PROPERTIES_SUFFIX);
        }
        return candidates;
    }

    private static Map<String, String> immutableMessages(Map<String, String> messages) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(messages));
    }

    private record CacheKey(String locale, String namespace) {
    }
}
