package com.tianrenservice.ai_framework_spring.core.entity;

import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.spi.ScopeIdentifier;
import com.tianrenservice.ai_framework_spring.core.vo.BusinessDealVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务实体基类 - 承载业务逻辑的核心容器
 *
 * 兼容性改动: getScopeEnum() → getScopeIdentifier()
 * 原返回 ScopeEnum 枚举，现返回 ScopeIdentifier 接口
 */
@Slf4j
@Getter
public class BusinessEntity<O extends BusinessHelper<?, ?>> extends UserBusiness {

    protected final O businessHelper;

    public BusinessEntity(O businessHelper) {
        super(businessHelper.getUserId());
        this.businessHelper = businessHelper;
    }

    public long getNowTime() {
        return businessHelper.getNowTime();
    }

    /**
     * 返回作用域标识
     * 兼容性改动: 原方法名 getScopeEnum()，返回类型从 ScopeEnum 改为 ScopeIdentifier
     * 默认返回 null，子类按需覆写
     */
    public ScopeIdentifier getScopeIdentifier() {
        return null;
    }

    public <V extends BusinessDealVO<T>, T extends BusinessEntity<?>> V buildVO(Class<V> clazz) {
        try {
            return clazz.getConstructor(UserBusiness.class).newInstance(this).build((T) this);
        } catch (Exception e) {
            throw new InterruptException("构造处理结果失败", e);
        }
    }

    public void afterProcess() {
        getBusinessHelper().saveDB();
        getBusinessHelper().delRedis();
    }

    public void finish() {
        businessHelper.finish();
    }
}
