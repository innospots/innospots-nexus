package com.innospots.nexus.console.i18n.loader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.i18n.domain.enums.ConsoleI18nNamespace;
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
 * @see ConsoleI18nNamespace
 */
public final class MessageBundleLoader {

    private static final String RESOURCE_ROOT = "i18n/console/";
    private static final String RESOURCE_FILE = "messages";
    private static final String PROPERTIES_SUFFIX = ".properties";

    private final ClassLoader classLoader;
    private final ConcurrentMap<CacheKey, MessageBundleVo> cache = new ConcurrentHashMap<>();

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
     * 加载指定 locale 与 namespace 的语言包。
     *
     * @param locale 规范语言代码
     * @param namespace 语言包命名空间
     * @return 不可变语言包快照
     */
    public MessageBundleVo load(String locale, ConsoleI18nNamespace namespace) {
        CacheKey cacheKey = new CacheKey(locale, namespace);
        return cache.computeIfAbsent(cacheKey, this::readBundle);
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

    private Map<String, String> readMessages(String locale, ConsoleI18nNamespace namespace) {
        String keyPrefix = namespace.code() + ".";
        Map<String, String> sorted = new TreeMap<>();
        String baseResource = RESOURCE_ROOT + namespace.code() + "/"
                + RESOURCE_FILE + PROPERTIES_SUFFIX;
        readResource(baseResource, keyPrefix, sorted);
        for (String candidate : localizedResources(locale)) {
            String resource = RESOURCE_ROOT + namespace.code() + "/" + candidate;
            readResource(resource, keyPrefix, sorted);
        }
        if (!sorted.isEmpty()) {
            return immutableMessages(sorted);
        }
        throw ConsoleExceptions.build(I18nStatusCode.BUNDLE_NOT_FOUND);
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
        }
        return candidates;
    }

    private static Map<String, String> immutableMessages(Map<String, String> messages) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(messages));
    }

    private record CacheKey(String locale, ConsoleI18nNamespace namespace) {
    }
}
