package com.tianrenservice.ai_framework_spring.core.testcase;

import com.tianrenservice.ai_framework_spring.core.constant.BusinessMode;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.pipeline.BusinessAssembly;
import com.tianrenservice.ai_framework_spring.core.spi.JsonSerializer;
import com.tianrenservice.ai_framework_spring.core.spi.TestCasePersistenceService;
import com.tianrenservice.ai_framework_spring.core.testcase.model.TestCaseVO;
import com.tianrenservice.ai_framework_spring.core.vo.BusinessVO;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试用例引擎 - 从 AdvertTestCaseService 抽象而来
 *
 * 兼容性改动:
 * - 移除 AdvertService 硬依赖 → 通过 TestCaseRunner 注入通用 service
 * - 移除 BusinessTestCaseServiceRpc (Dubbo) → TestCasePersistenceService SPI
 * - 移除 JsonUtil → JsonSerializer SPI
 * - 移除 Ret<T> 包装 → 直接返回结果
 * - 移除 javafx.util.Pair / ImmutableMap → 标准 JDK 实现
 */
@Slf4j
public class TestCaseEngine {

    private final TestCaseRunner runner;
    private final TestCaseComparator comparator;
    private final TestCasePersistenceService persistenceService;
    private final JsonSerializer jsonSerializer;

    public TestCaseEngine(Object service, String methodName,
                          TestCasePersistenceService persistenceService,
                          JsonSerializer jsonSerializer) {
        this.persistenceService = persistenceService;
        this.jsonSerializer = jsonSerializer;
        this.runner = new TestCaseRunner(service, methodName, persistenceService);
        this.comparator = new TestCaseComparator(jsonSerializer);
    }

    /**
     * 录制测试用例 - 执行业务流程并记录所有交互
     */
    public TestCaseVO record(TestCaseVO testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        BusinessAssembly assembly = runner.run(BusinessMode.RECORD, testCase);
        return runner.generateSnapshot(assembly);
    }

    /**
     * 检查测试用例 - 根据 ID 加载并校验
     */
    public boolean check(Integer testCaseId) {
        TestCaseVO checkCase = persistenceService.load(testCaseId);
        if (checkCase == null) {
            throw new InterruptException("Failed to retrieve test case: " + testCaseId);
        }
        BusinessAssembly assembly = runner.run(BusinessMode.CHECK, checkCase);
        TestCaseVO resultCase = runner.generateSnapshot(assembly);
        Map<String, String> inconsistentMap = new HashMap<>();
        if (comparator.equalsCase(checkCase, resultCase, inconsistentMap)) {
            return true;
        }
        throw new InterruptException("Test case check failed: " + testCaseId
                + ", inconsistencies: " + jsonSerializer.toJson(inconsistentMap));
    }

    /**
     * 复盘测试用例 - 使用提供的用例数据进行验证
     */
    public boolean review(TestCaseVO testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        BusinessAssembly assembly = runner.run(BusinessMode.REVIEW, testCase);
        TestCaseVO resultCase = runner.generateSnapshot(assembly);
        Map<String, String> inconsistentMap = new HashMap<>();
        if (comparator.equalsCase(testCase, resultCase, inconsistentMap)) {
            return true;
        }
        throw new InterruptException("Test case review failed, inconsistencies: "
                + jsonSerializer.toJson(inconsistentMap));
    }
    // PLACEHOLDER_REPLAY

    /**
     * 重播测试用例 - 使用录制数据回放，未匹配的调用直接执行
     */
    public TestCaseVO replay(TestCaseVO testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        BusinessAssembly assembly = runner.run(BusinessMode.REPLAY, testCase);
        return runner.generateSnapshot(assembly);
    }

    /**
     * 重生成测试用例 - 重新执行并更新持久化数据
     */
    public TestCaseVO regenerate(Integer testCaseId, TestCaseVO testCase) {
        if (testCaseId == null || testCase == null) {
            throw new IllegalArgumentException("Test case id and test case cannot be null");
        }
        testCase.setId(testCaseId);
        BusinessAssembly assembly = runner.run(BusinessMode.REGENERATE, testCase);
        BusinessVO businessVO = runner.extractFirstBusinessVO(assembly);
        TestCaseVO resultCase = assembly.generateTestCase(businessVO);
        resultCase.setId(testCaseId);
        Map<String, String> inconsistentMap = new HashMap<>();
        if (comparator.equalsCase(testCase, resultCase, inconsistentMap)) {
            assembly.updateTestCase(businessVO);
            return resultCase;
        }
        throw new InterruptException("Test case regenerate failed, inconsistencies: "
                + jsonSerializer.toJson(inconsistentMap));
    }

    /**
     * 获取比较器（供外部自定义比较逻辑使用）
     */
    public TestCaseComparator getComparator() {
        return comparator;
    }
}