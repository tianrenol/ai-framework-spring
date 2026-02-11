package com.tianrenservice.ai_framework_spring.core.testcase;

import com.tianrenservice.ai_framework_spring.core.constant.BusinessMode;
import com.tianrenservice.ai_framework_spring.core.exception.InterruptException;
import com.tianrenservice.ai_framework_spring.core.pipeline.BusinessAssembly;
import com.tianrenservice.ai_framework_spring.core.spi.TestCasePersistenceService;
import com.tianrenservice.ai_framework_spring.core.testcase.model.TestCaseVO;
import com.tianrenservice.ai_framework_spring.core.vo.BusinessVO;
import lombok.extern.slf4j.Slf4j;

/**
 * 测试用例执行器 - 封装 BusinessAssembly.runTestCase 的调用
 * 提供统一的执行入口，屏蔽模式切换细节
 */
@Slf4j
public class TestCaseRunner {

    private final Object service;
    private final String methodName;
    private final TestCasePersistenceService persistenceService;

    /**
     * @param service           业务服务实例（如 AdvertService）
     * @param methodName        服务方法名（如 "handle"）
     * @param persistenceService 测试用例持久化服务
     */
    public TestCaseRunner(Object service, String methodName, TestCasePersistenceService persistenceService) {
        this.service = service;
        this.methodName = methodName;
        this.persistenceService = persistenceService;
    }

    /**
     * 以指定模式执行测试用例
     * @return 执行后的 BusinessAssembly（包含执行结果数据）
     */
    public BusinessAssembly run(BusinessMode mode, TestCaseVO testCaseVO) {
        BusinessAssembly assembly = BusinessAssembly.runTestCase(mode, service, methodName, testCaseVO, persistenceService);
        if (assembly == null) {
            throw new InterruptException("执行测试用例失败，未找到匹配的组合线类型: " + testCaseVO.getBusinessType());
        }
        return assembly;
    }

    /**
     * 从执行后的 Assembly 中提取第一个 BusinessVO
     */
    public BusinessVO extractFirstBusinessVO(BusinessAssembly assembly) {
        return assembly.getMultiBusinessVO().values().stream()
                .findFirst()
                .orElseThrow(() -> new InterruptException("测试用例中未找到业务数据"));
    }

    /**
     * 从执行后的 Assembly 中生成测试用例快照
     */
    public TestCaseVO generateSnapshot(BusinessAssembly assembly) {
        BusinessVO businessVO = extractFirstBusinessVO(assembly);
        return assembly.generateTestCase(businessVO);
    }
}
