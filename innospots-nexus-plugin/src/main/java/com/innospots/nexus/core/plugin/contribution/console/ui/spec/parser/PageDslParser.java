package com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

/**
 * 解析与序列化 Pactor 页面 DSL 文档。
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface PageDslParser {

    /**
     * 将 YAML 内容解析为已校验的页面 DSL 文档。
     *
     * @param content YAML 内容
     * @return 页面 DSL 文档
     */
    PageDsl parse(String content);

    /**
     * 将已校验的页面 DSL 文档序列化为 YAML。
     *
     * @param document 页面 DSL 文档
     * @return YAML 字符串
     */
    String write(PageDsl document);
}
