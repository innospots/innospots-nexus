package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 由 {@link PageDsl#requires} 声明的运行时与组件能力要求。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RequiresConfig {

    /** 必填运行时语义版本范围，例如 {@code >=0.1.0}。 */
    private String runtime;

    /** 按组件类型名称索引的必填组件版本。 */
    private Map<String, String> components = new LinkedHashMap<>();
}
