package com.innospots.nexus.base.ui.spec.parser;

import com.innospots.nexus.base.ui.spec.PageDsl;

/** Parses and serializes Pactor page DSL documents. */
public interface PageDslParser {

    /** Parses YAML content into a validated page DSL document. */
    PageDsl parse(String content);

    /** Serializes a validated page DSL document as YAML. */
    String write(PageDsl document);
}
