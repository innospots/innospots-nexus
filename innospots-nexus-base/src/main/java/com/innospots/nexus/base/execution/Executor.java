package com.innospots.nexus.base.execution;

/**
 * Core execution unit interface. Each executor has a unique identifier
 * and is invoked with an {@link ExecutionContext} to produce an output.
 *
 * @param <O> the output type produced by this executor
 * @param <C> the context type consumed by this executor
 */
public interface Executor<O, C extends ExecutionContext> {

    String identifier();

    O execute(C context);

    default String info() {
        return identifier();
    }
}
