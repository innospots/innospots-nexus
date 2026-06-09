package com.innospots.nexus.console.extension;

/**
 * Extension contract for management-console capabilities.
 * <p>
 * Console extensions declare navigation and route metadata only. Domain logic,
 * persistence, runtime wiring, and framework adapters belong outside this
 * module.
 * </p>
 */
public interface ConsoleExtension {

    /**
     * Returns the extension contribution that should be mounted by a console
     * runtime.
     *
     * @return immutable console contribution metadata
     */
    ConsoleContribution contribution();
}
