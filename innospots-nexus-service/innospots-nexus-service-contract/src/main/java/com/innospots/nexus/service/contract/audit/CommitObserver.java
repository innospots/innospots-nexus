package com.innospots.nexus.service.contract.audit;

import java.util.function.Consumer;

/**
 * Observes host transaction completion for REQUIRED audit.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CommitState
 */
public interface CommitObserver {

    /**
     * Returns the current commit state.
     *
     * @return state
     */
    CommitState currentState();

    /**
     * Registers a callback invoked after completion.
     *
     * @param callback completion callback
     */
    void afterCompletion(Consumer<CommitState> callback);
}
