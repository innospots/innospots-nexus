package com.innospots.nexus.base.execution;

/**
 * 核心执行单元接口。每个执行器拥有唯一标识符，
 * 接收 {@link ExecutionContext} 并产出输出结果。
 *
 * @author Smars
 * @date 2026/09/13
 * @param <O> 执行器产出的输出类型
 * @param <C> 执行器消费的上下文类型
 * @see ExecutionContext
 * @see ExecutionRecord
 */
public interface Executor<O, C extends ExecutionContext> {

    /**
     * 返回执行器唯一标识符。
     *
     * @return 标识符
     */
    String identifier();

    /**
     * 在给定上下文中执行并返回输出。
     *
     * @param context 执行上下文
     * @return 执行输出
     */
    O execute(C context);

    /**
     * 返回执行器的描述信息（默认为标识符）。
     *
     * @return 描述信息
     */
    default String info() {
        return identifier();
    }
}
