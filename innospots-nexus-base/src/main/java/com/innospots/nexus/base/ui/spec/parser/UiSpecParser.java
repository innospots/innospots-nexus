package com.innospots.nexus.base.ui.spec.parser;

import com.innospots.nexus.base.ui.spec.UiSpec;

/** Parses and serializes YAML page specifications independently of their storage. */
public interface UiSpecParser {

    /** Parses YAML content. */
    UiSpec parse(String content);

    /** Serializes a specification as YAML. */
    String write(UiSpec spec);
}
