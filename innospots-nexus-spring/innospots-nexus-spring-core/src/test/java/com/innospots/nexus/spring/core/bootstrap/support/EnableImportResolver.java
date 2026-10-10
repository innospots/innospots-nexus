package com.innospots.nexus.spring.core.bootstrap.support;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.context.annotation.Import;

/**
 * 解析 {@code @Enable*} 组合注解链上声明的 {@link Import} 配置类。
 */
public final class EnableImportResolver {

    private EnableImportResolver() {
    }

    /**
     * 收集从给定 enable 注解类型起、沿元注解展开的全部 {@code @Import} 配置类。
     *
     * @param enableAnnotation enable 注解类型
     * @return 去重后的配置类集合（保持大致发现顺序）
     */
    public static Set<Class<?>> resolveImportedConfigurationTypes(Class<? extends Annotation> enableAnnotation) {
        Set<Class<?>> imported = new LinkedHashSet<>();
        Set<Class<?>> visited = new LinkedHashSet<>();
        walkAnnotationType(enableAnnotation, imported, visited);
        return imported;
    }

    private static void walkAnnotationType(
            Class<?> annotationType,
            Set<Class<?>> imported,
            Set<Class<?>> visited) {
        if (!visited.add(annotationType)) {
            return;
        }
        Import importAnnotation = annotationType.getAnnotation(Import.class);
        if (importAnnotation != null) {
            imported.addAll(Arrays.asList(importAnnotation.value()));
        }
        for (Annotation metaAnnotation : annotationType.getAnnotations()) {
            Class<? extends Annotation> metaType = metaAnnotation.annotationType();
            if (metaType.getName().startsWith("java.lang.annotation")) {
                continue;
            }
            walkAnnotationType(metaType, imported, visited);
        }
    }
}
