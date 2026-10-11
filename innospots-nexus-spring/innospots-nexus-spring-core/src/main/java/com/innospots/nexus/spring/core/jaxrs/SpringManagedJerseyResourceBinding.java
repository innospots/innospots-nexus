package com.innospots.nexus.spring.core.jaxrs;

/**
 * Spring 容器中的 JAX-RS 根资源实例与其声明类型。
 */
record SpringManagedJerseyResourceBinding(Object instance, Class<?> resourceClass) {
}
