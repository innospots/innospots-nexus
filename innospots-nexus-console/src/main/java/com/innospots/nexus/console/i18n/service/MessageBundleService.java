package com.innospots.nexus.console.i18n.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.i18n.domain.vo.MessageBundleSetVo;
import com.innospots.nexus.console.i18n.domain.vo.MessageBundleVo;
import com.innospots.nexus.console.i18n.loader.MessageBundleLoader;
import com.innospots.nexus.console.i18n.status.I18nStatusCode;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 协商请求语言、校验协议参数并组装 Console 语言包集合。
 *
 * @author Smars
 * @date 2026/10/26
 * @see MessageBundleLoader
 */
public final class MessageBundleService {

    /** 当前应用标识。 */
    public static final String APP_ID = "console";
    /** 默认语言。 */
    public static final String DEFAULT_LOCALE = "zh-CN";
    /** 语言包协议版本。 */
    public static final String FORMAT = "simple-v1";

    private static final String ENGLISH_LOCALE = "en-US";
    private static final String DEFAULT_RELEASE = "build-release";
    private static final Pattern RELEASE_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private final MessageBundleLoader loader;

    /**
     * 创建语言包服务。
     *
     * @param loader 语言包资源加载器
     */
    public MessageBundleService(MessageBundleLoader loader) {
        if (loader == null) {
            throw ConsoleExceptions.build(
                    I18nStatusCode.BUNDLE_LOAD_FAILED,
                    "Message bundle loader is required");
        }
        this.loader = loader;
    }

    /**
     * 加载语言包集合。
     *
     * <p>显式 query locale 优先，其次为 {@code locale} cookie、
     * {@code Accept-Language}，最后回退到 {@link #DEFAULT_LOCALE}。</p>
     *
     * @param appId 应用标识
     * @param locale 显式语言
     * @param cookieLocale Cookie 中的语言偏好
     * @param acceptLanguage HTTP Accept-Language
     * @param namespaces 逗号分隔 namespace；空值加载全部
     * @param release 发布标识；空值使用稳定构建标识
     * @return 语言包集合
     */
    public MessageBundleSetVo load(
            String appId,
            String locale,
            String cookieLocale,
            String acceptLanguage,
            String namespaces,
            String release
    ) {
        requireApplication(appId);
        String resolvedLocale = resolveLocale(locale, cookieLocale, acceptLanguage);
        Set<String> selectedNamespaces = resolveNamespaces(namespaces);
        String resolvedRelease = resolveRelease(release);

        Map<String, MessageBundleVo> bundles = new LinkedHashMap<>();
        Set<String> keyOwners = new LinkedHashSet<>();
        for (String namespace : selectedNamespaces) {
            MessageBundleVo bundle = loader.load(resolvedLocale, namespace);
            for (String key : bundle.messages().keySet()) {
                if (!keyOwners.add(key)) {
                    throw ConsoleExceptions.build(
                            I18nStatusCode.BUNDLE_LOAD_FAILED,
                            "Duplicate cross-namespace message key: " + key);
                }
            }
            bundles.put(namespace, bundle);
        }
        return new MessageBundleSetVo(
                APP_ID,
                resolvedLocale,
                resolvedRelease,
                FORMAT,
                bundles);
    }

    private static void requireApplication(String appId) {
        String normalized = appId == null ? "" : appId.trim();
        if (!normalized.isEmpty() && !APP_ID.equals(normalized)) {
            throw ConsoleExceptions.build(I18nStatusCode.APP_ID_NOT_SUPPORTED);
        }
    }

    private static String resolveLocale(String queryLocale, String cookieLocale, String acceptLanguage) {
        if (queryLocale != null && !queryLocale.isBlank()) {
            return canonicalize(queryLocale)
                    .orElseThrow(() -> ConsoleExceptions.build(I18nStatusCode.LOCALE_NOT_SUPPORTED));
        }
        if (cookieLocale != null && !cookieLocale.isBlank()) {
            String canonical = canonicalize(cookieLocale).orElse(null);
            if (canonical != null) {
                return canonical;
            }
        }
        return acceptedLocale(acceptLanguage).orElse(DEFAULT_LOCALE);
    }

    private static java.util.Optional<String> acceptedLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return java.util.Optional.empty();
        }
        return Arrays.stream(acceptLanguage.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(AcceptedLanguage::parse)
                .filter(value -> value.quality() > 0)
                .sorted(Comparator.comparingDouble(AcceptedLanguage::quality).reversed())
                .map(AcceptedLanguage::locale)
                .map(MessageBundleService::canonicalize)
                .flatMap(java.util.Optional::stream)
                .findFirst();
    }

    private static java.util.Optional<String> canonicalize(String locale) {
        String normalized = locale == null ? "" : locale.trim().replace('_', '-').toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return java.util.Optional.empty();
        }
        if ("zh".equals(normalized) || "zh-cn".equals(normalized)) {
            return java.util.Optional.of(DEFAULT_LOCALE);
        }
        if ("en".equals(normalized) || "en-us".equals(normalized)) {
            return java.util.Optional.of(ENGLISH_LOCALE);
        }
        return java.util.Optional.empty();
    }

    private Set<String> resolveNamespaces(String namespaces) {
        List<String> available = loader.namespaces();
        if (namespaces == null || namespaces.isBlank()) {
            return new LinkedHashSet<>(available);
        }
        Set<String> selected = new LinkedHashSet<>();
        for (String code : namespaces.split(",")) {
            String normalized = code.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            if (!available.contains(normalized)) {
                throw ConsoleExceptions.build(I18nStatusCode.NAMESPACE_NOT_SUPPORTED);
            }
            selected.add(normalized);
        }
        if (selected.isEmpty()) {
            return new LinkedHashSet<>(available);
        }
        return selected;
    }

    private static String resolveRelease(String release) {
        if (release == null || release.isBlank()) {
            return implementationVersion();
        }
        String normalized = release.trim();
        if (!RELEASE_PATTERN.matcher(normalized).matches()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        return normalized;
    }

    private static String implementationVersion() {
        Package servicePackage = MessageBundleService.class.getPackage();
        String version = servicePackage == null ? null : servicePackage.getImplementationVersion();
        return version == null || version.isBlank() ? DEFAULT_RELEASE : version;
    }

    private record AcceptedLanguage(String locale, double quality) {

        private static AcceptedLanguage parse(String value) {
            String[] parts = value.split(";");
            double quality = 1.0;
            for (int index = 1; index < parts.length; index++) {
                String parameter = parts[index].trim();
                if (parameter.startsWith("q=")) {
                    try {
                        quality = Double.parseDouble(parameter.substring(2));
                    } catch (NumberFormatException ignored) {
                        quality = 0;
                    }
                }
            }
            return new AcceptedLanguage(parts[0].trim(), quality);
        }
    }
}
