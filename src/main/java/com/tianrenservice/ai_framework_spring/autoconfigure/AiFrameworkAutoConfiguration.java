package com.tianrenservice.ai_framework_spring.autoconfigure;

import com.tianrenservice.ai_framework_spring.autoconfigure.spi.DefaultTypeRegistry;
import com.tianrenservice.ai_framework_spring.autoconfigure.spi.JacksonJsonSerializer;
import com.tianrenservice.ai_framework_spring.autoconfigure.spi.SpringBeanProvider;
import com.tianrenservice.ai_framework_spring.core.entity.BusinessHelper;
import com.tianrenservice.ai_framework_spring.core.pipeline.BusinessAssembly;
import com.tianrenservice.ai_framework_spring.core.record.aspect.RecordAndReplayAspect;
import com.tianrenservice.ai_framework_spring.core.spi.BeanProvider;
import com.tianrenservice.ai_framework_spring.core.spi.JsonSerializer;
import com.tianrenservice.ai_framework_spring.core.spi.TypeRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * AI Framework Spring Boot 自动配置
 *
 * 自动装配所有 SPI 默认实现，并完成框架初始化：
 * - JsonSerializer → JacksonJsonSerializer
 * - BeanProvider → SpringBeanProvider
 * - TypeRegistry → DefaultTypeRegistry
 * - RecordAndReplayAspect → AOP 切面
 *
 * 业务方可通过自定义 Bean 覆盖任意默认实现
 */
@AutoConfiguration
@EnableConfigurationProperties(AiFrameworkProperties.class)
public class AiFrameworkAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JsonSerializer jsonSerializer() {
        return new JacksonJsonSerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public BeanProvider beanProvider(ApplicationContext applicationContext) {
        SpringBeanProvider provider = new SpringBeanProvider(applicationContext);
        // 框架初始化：注入 BeanProvider 到 BusinessHelper
        BusinessHelper.configureBeanProvider(provider);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeRegistry typeRegistry(JsonSerializer jsonSerializer) {
        DefaultTypeRegistry registry = new DefaultTypeRegistry();
        // 框架初始化：注入 TypeRegistry 和 JsonSerializer 到 BusinessAssembly
        BusinessAssembly.configure(registry, jsonSerializer);
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "ai-framework", name = "aspect-enabled", havingValue = "true", matchIfMissing = true)
    public RecordAndReplayAspect recordAndReplayAspect(JsonSerializer jsonSerializer) {
        return new RecordAndReplayAspect(jsonSerializer);
    }
}
