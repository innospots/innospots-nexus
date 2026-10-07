package com.innospots.nexus.base.json;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 激活 {@link I18nObject} 序列化/反序列化及 {@code @I18n} 字段契约的 Jackson {@link Module}。
 * <p>
 * 将此模块注册到 {@code ObjectMapper} 后，{@link Jsons} 默认可读写 I18nObject 的
 * 单语言字符串与多语言对象两种 JSON 形态。
 *
 * <pre>{@code
 * ObjectMapper mapper = JsonMapper.builder()
 *     .addModule(new I18nModule())
 *     .build();
 * }</pre>
 *
 * @author Smars
 * @date 2026/09/16
 * @see I18nObjectSerializer
 * @see I18nObjectDeserializer
 * @see com.innospots.nexus.base.i18n.I18n
 */
public final class I18nModule extends SimpleModule {

    public I18nModule() {
        super("innospots-nexus-i18n", new Version(0, 1, 0, null, "com.innospots", "innospots-nexus-i18n"));
        addSerializer(I18nObject.class, I18nObjectSerializer.getInstance());
        addDeserializer(I18nObject.class, I18nObjectDeserializer.getInstance());
    }
}
