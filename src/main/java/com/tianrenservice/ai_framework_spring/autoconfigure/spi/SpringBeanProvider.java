package com.tianrenservice.ai_framework_spring.autoconfigure.spi;

import com.tianrenservice.ai_framework_spring.core.spi.BeanProvider;
import org.springframework.context.ApplicationContext;

/**
 * BeanProvider 的 Spring ApplicationContext 默认实现
 */
public class SpringBeanProvider implements BeanProvider {

    private final ApplicationContext applicationContext;

    public SpringBeanProvider(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }
}
