package com.tianrenservice.ai_framework_spring.core.pipeline;

import com.tianrenservice.ai_framework_spring.core.entity.BusinessEntity;
import com.tianrenservice.ai_framework_spring.core.entity.BusinessHelper;
import com.tianrenservice.ai_framework_spring.core.exception.DegradeException;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.exception.SkipException;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessDealVO;
import com.tianrenservice.ai_framework_spring.core.vo.UserBusinessVO;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 业务门面基类 - 模板方法模式驱动 Pipeline 执行
 *
 * 兼容性改动: 移除 AdvertEnvUtil.isOnline() 依赖
 * 日志输出不再区分线上/线下环境
 */
@Slf4j
public abstract class BusinessFacade<V extends UserBusinessDealVO<T>, T extends BusinessEntity<?>,
        O extends BusinessHelper<R, A>, R extends UserBusinessVO, A extends BusinessAssembly> {

    public abstract O getBusinessHelper();

    @SuppressWarnings("unchecked")
    public static <T extends BusinessEntity<?>, O extends BusinessHelper<R, A>,
            R extends UserBusinessVO, A extends BusinessAssembly>
    T buildForContext(BusinessContext<R, A> advertContext, O advertHelper, Class<T> clazz) {
        advertHelper.setBusinessContext(advertContext);
        return BusinessHelper.build(advertHelper, clazz);
    }

    @SuppressWarnings("unchecked")
    public T build(O o, R r, A a) {
        Class genericSuperclass = (Class) this.getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass.getGenericSuperclass();
        Type[] actualClassArguments = parameterizedType.getActualTypeArguments();
        Class<T> tClazz = (Class) actualClassArguments[1];
        BusinessContext<R, A> advertContext = BusinessContext.build(r, a);
        return buildForContext(advertContext, o, tClazz);
    }

    @SuppressWarnings("unchecked")
    public V process(O o, R r, A a) {
        try {
            Class realClass = (Class) this.getClass().getGenericSuperclass();
            ParameterizedType parameterizedType = (ParameterizedType) realClass.getGenericSuperclass();
            Type[] actualClassArguments = parameterizedType.getActualTypeArguments();
            Class<V> vClazz = (Class) actualClassArguments[0];
            Class<T> tClazz = (Class) actualClassArguments[1];
            try {
                a.build(tClazz, vClazz, r);
            } catch (Exception e) {
                log.error("businessFacade process error, userId:{}, {}:{}", r.getUserId(), vClazz.getName(), e.getMessage());
                throw new InterruptException("组合线数据注入中断", e);
            }
            BusinessContext<R, A> advertContext = BusinessContext.build(r, a);
            T t = buildForContext(advertContext, o, tClazz);
            a.ready(t, r);
            doProcess(t);
            V v = t.buildVO(vClazz);
            t.afterProcess();
            a.complete(v, t, r);
            log.debug("businessFacade process end, userId:{}, {}", r.getUserId(), vClazz.getName());
            return v;
        } catch (SkipException e) {
            log.error("businessFacade process skip, userId:{}, message:{}", r.getUserId(), e.getMessage());
            return null;
        } catch (DegradeException e) {
            log.error("businessFacade process degrade, userId:{}, message:{}", r.getUserId(), e.getMessage());
            return null;
        } catch (Exception e) {
            throw new InterruptException("businessFacade process interrupt", e);
        }
    }

    public abstract void doProcess(T t);
}
