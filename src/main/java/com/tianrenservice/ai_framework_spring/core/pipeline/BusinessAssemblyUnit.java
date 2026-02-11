package com.tianrenservice.ai_framework_spring.core.pipeline;

import com.tianrenservice.ai_framework_spring.core.entity.BusinessEntity;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessDealVO;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;
import lombok.Getter;

import java.util.Objects;

/**
 * 组合线单元 - Pipeline 中的单个执行节点
 */
@Getter
public class BusinessAssemblyUnit<V extends UserBusinessDealVO<?>, T extends BusinessEntity<?>, R extends UserBusinessVO, A extends BusinessAssembly> {

    private final BusinessContext<R, A> businessContext;
    private T businessEntity;
    private V businessDealVO;
    private final int order;

    public BusinessAssemblyUnit(BusinessContext<R, A> businessContext, T businessEntity, V businessDealVO, int order) {
        this.order = order;
        if (Objects.isNull(businessContext)) {
            throw new InterruptException("BusinessContext cannot be null");
        }
        this.businessContext = businessContext;
        this.businessEntity = businessEntity;
        this.businessDealVO = businessDealVO;
    }

    @SuppressWarnings("unchecked")
    public void ready(BusinessEntity<?> t) {
        if (Objects.nonNull(businessEntity)) {
            throw new InterruptException("businessEntity is already set, cannot be overwritten.");
        }
        this.businessEntity = (T) t;
    }

    public boolean isReady() {
        return Objects.nonNull(businessEntity);
    }

    @SuppressWarnings("unchecked")
    public void complete(UserBusinessDealVO<?> v) {
        if (Objects.nonNull(businessDealVO)) {
            throw new InterruptException("BusinessDealVO is already set, cannot be overwritten.");
        }
        this.businessDealVO = (V) v;
    }

    public boolean isComplete() {
        return Objects.nonNull(businessDealVO);
    }

    /**
     * 兼容性改动: 原实现使用 assemblyEnum.name() + businessEnum.name()
     * 现改为通过接口 getCode() 获取标识
     */
    public String getMarkName() {
        return businessContext.getAssembly().getAssemblyTypeCode()
                + "-" + businessContext.getBusinessVo().getBusinessType().getCode()
                + "-" + order;
    }

    public static <V extends UserBusinessDealVO<?>, T extends BusinessEntity<?>, R extends UserBusinessVO, A extends BusinessAssembly>
    BusinessAssemblyUnit<V, T, R, A> doBuild(Class<V> vClass, Class<T> tClass, R r, A a) {
        return new BusinessAssemblyUnit<>(BusinessContext.build(r, a), null, null, a.getUnitCount());
    }

    public static <V extends UserBusinessDealVO<?>, T extends BusinessEntity<?>, R extends UserBusinessVO, A extends BusinessAssembly>
    BusinessAssemblyUnit<V, T, R, A> doBuild(V v, T t, R r, A a) {
        return new BusinessAssemblyUnit<>(BusinessContext.build(r, a), t, v, a.getUnitCount());
    }
}