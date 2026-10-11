package com.innospots.nexus.spring.core.jaxrs;

import java.util.List;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * 将 Spring 单例端点绑定到 Jersey 类注册，避免 {@code ResourceConfig#register(Object)} 触发
 * provider 运行时误报（Jersey #3700）。
 */
final class SpringManagedJerseyResourceBinder extends AbstractBinder {

    private final List<SpringManagedJerseyResourceBinding> bindings;

    SpringManagedJerseyResourceBinder(List<SpringManagedJerseyResourceBinding> bindings) {
        this.bindings = bindings;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        for (SpringManagedJerseyResourceBinding binding : bindings) {
            Class<? super Object> resourceType = (Class<? super Object>) binding.resourceClass();
            bind(binding.instance()).to(resourceType);
        }
    }
}
