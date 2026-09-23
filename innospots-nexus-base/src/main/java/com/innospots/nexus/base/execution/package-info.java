/**
 * 为控制台操作与工作流步骤提供统一执行标准的预留执行器 SPI。
 *
 * <p>本包中的类型仅定义契约（{@link com.innospots.nexus.base.execution.Executor}、
 * {@link com.innospots.nexus.base.execution.ExecutionContext}、
 * {@link com.innospots.nexus.base.execution.ExecutionRecord}）。
 * 尚无注册表或默认实现；在首个消费方出现时于 core 或 console 中添加编排逻辑。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.execution.Executor
 */
package com.innospots.nexus.base.execution;
