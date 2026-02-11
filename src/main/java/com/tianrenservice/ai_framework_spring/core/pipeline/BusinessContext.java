package com.tianrenservice.ai_framework_spring.core.pipeline;

import com.tianrenservice.ai_framework_spring.core.entity.BusinessEntity;
import com.tianrenservice.ai_framework_spring.core.entity.BusinessHelper;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessDealVO;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 业务上下文 - 持有当前业务的输入 VO 和组合线引用
 */
@Getter
@Slf4j
@Builder
public class BusinessContext<T extends UserBusinessVO, A extends BusinessAssembly> {

    private final T businessVo;
    private final A assembly;

    @SuppressWarnings("unchecked")
    public static <T extends UserBusinessVO, A extends BusinessAssembly> BusinessContext<T, A> build(T businessVo, A assembly) {
        return (BusinessContext<T, A>) BusinessContext.builder()
                .businessVo(businessVo)
                .assembly(Objects.nonNull(assembly) ? assembly : BusinessAssembly.createAssembly(BusinessEmptyAssembly.class))
                .build();
    }

    public String getUserId() {
        return businessVo.getUserId();
    }
}
