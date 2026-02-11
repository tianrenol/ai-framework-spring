package com.tianrenservice.ai_framework_spring.core.record.aspect;

import com.tianrenservice.ai_framework_spring.core.constant.BusinessMode;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.record.annotation.RecordAndReplay;
import com.tianrenservice.ai_framework_spring.core.record.model.BusinessEnv;
import com.tianrenservice.ai_framework_spring.core.record.model.InteractionRecord;
import com.tianrenservice.ai_framework_spring.core.spi.JsonSerializer;
import com.tianrenservice.ai_framework_spring.core.util.BeanUtil;
import com.tianrenservice.ai_framework_spring.core.util.CacheInvoke;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 录制/回放 AOP 切面
 *
 * 兼容性改动:
 * - 移除 AdvertEnvUtil 依赖
 * - 移除 LogConstant 依赖
 * - JsonUtil → JsonSerializer SPI
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class RecordAndReplayAspect {

    @Delegate
    private final CacheInvoke cacheInvoke = new CacheInvoke(true);

    private final JsonSerializer jsonSerializer;

    @Pointcut("@within(com.tianrenservice.ai_framework_spring.core.record.annotation.RecordAndReplay) || @annotation(com.tianrenservice.ai_framework_spring.core.record.annotation.RecordAndReplay)")
    public void recordAndReplayPointcut() {
    }

    @Around("recordAndReplayPointcut()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        List<Object> args = new ArrayList<>(Arrays.asList(joinPoint.getArgs()));

        RecordAndReplay annotation = Optional.ofNullable(method.getAnnotation(RecordAndReplay.class))
                .orElse(joinPoint.getTarget().getClass().getAnnotation(RecordAndReplay.class));
        String key = (annotation == null || annotation.value().isEmpty()) ? method.getName() : annotation.value();

        if ("ignore".equals(key)) {
            return joinPoint.proceed();
        }

        BusinessEnv businessEnv = (BusinessEnv) joinPoint.getTarget();
        BusinessMode mode = businessEnv.getMode();

        if (mode == BusinessMode.LIVE) {
            return joinPoint.proceed();
        }

        if (annotation != null && !annotation.mark().isEmpty()) {
            args.replaceAll(arg -> cacheInvoke(businessEnv, annotation.mark(), Collections.singletonList(Object.class), arg));
        }
        args.replaceAll(this::getDefaultMark);

        log.info("方法{}进行交互: args={}", method.getName(), jsonSerializer.toJson(args));

        Object result = null;
        List<InteractionRecord> methodRecords;
        InteractionRecord record;
        boolean isVoidMethod = method.getReturnType().equals(Void.TYPE);

        switch (mode) {
            case RECORD:
                if (!isVoidMethod) {
                    result = joinPoint.proceed();
                } else {
                    joinPoint.proceed();
                }
                break;
            case CHECK:
            case REVIEW:
            case REGENERATE:
                methodRecords = businessEnv.getCovers().get(key);
                if (methodRecords == null || methodRecords.isEmpty()) {
                    log.error("未找到方法{}的记录", key);
                    throw new InterruptException("未找到方法" + key + "的记录");
                }
                if (isVoidMethod) {
                    businessEnv.findMatchingRecord(methodRecords, args);
                } else {
                    record = businessEnv.findMatchingRecord(methodRecords, args);
                    result = castResult(record, signature);
                }
                break;
            case REPLAY:
                methodRecords = businessEnv.getCovers().get(key);
                if (methodRecords == null || methodRecords.isEmpty()) {
                    log.warn("未找到方法{}的记录，直接执行", key);
                    result = joinPoint.proceed();
                    break;
                }
                record = businessEnv.findMatchingRecordIgnoreNoFind(methodRecords, args);
                if (record == null) {
                    log.warn("未找到方法{}的匹配记录，直接执行", key);
                    result = joinPoint.proceed();
                    break;
                }
                if (!isVoidMethod) {
                    result = castResult(record, signature);
                }
                break;
            default:
                throw new InterruptException("未知模式: " + mode);
        }

        record = businessEnv.findMatchingRecordIgnoreNoFind(businessEnv.getRecords().get(key), args);
        if (record == null) {
            record = new InteractionRecord(key, args, result);
            businessEnv.getRecords().computeIfAbsent(key, k -> new ArrayList<>()).add(record);
            log.info("方法{}新增交互记录: {}", key, jsonSerializer.toJson(record));
        }
        return result;
    }
    // PLACEHOLDER_METHODS

    private Object castResult(InteractionRecord record, MethodSignature signature) {
        Object result = record.getResult();
        Class<?> returnType = signature.getReturnType();
        if (result != null) {
            result = doCastResult(result, returnType);
            if (result == null) {
                throw new InterruptException("方法" + record.getMethodName()
                        + "记录的返回值=" + jsonSerializer.toJson(record.getResult()) + "，类型转换失败");
            }
        }
        return result;
    }

    public Object getDefaultMark(Object o) {
        if (o == null || BeanUtil.isPrimitive(o.getClass()) || o instanceof String || o instanceof Collection) {
            return o;
        }
        return jsonSerializer.toMap(jsonSerializer.toJson(o));
    }

    public Object doCastResult(Object result, Class<?> returnType) {
        if (result == null) return null;
        if (returnType.isAssignableFrom(result.getClass())) return result;
        if (returnType == Integer.class || returnType == int.class) return Integer.parseInt(result.toString());
        if (returnType == Long.class || returnType == long.class) return Long.parseLong(result.toString());
        if (returnType == Double.class || returnType == double.class) return Double.parseDouble(result.toString());
        if (returnType == Boolean.class || returnType == boolean.class) return Boolean.parseBoolean(result.toString());
        if (returnType == Float.class || returnType == float.class) return Float.parseFloat(result.toString());
        if (returnType == Short.class || returnType == short.class) return Short.parseShort(result.toString());
        if (returnType == Byte.class || returnType == byte.class) return Byte.parseByte(result.toString());
        if (returnType == Character.class || returnType == char.class) {
            if (result.toString().length() == 1) return result.toString().charAt(0);
            log.error("无法将结果转换为字符类型，结果长度不为1: {}", result);
            return null;
        }
        if (returnType == String.class) return result.toString();
        return jsonSerializer.fromJson(jsonSerializer.toJson(result), returnType);
    }
}