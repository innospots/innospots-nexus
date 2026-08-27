package com.innospots.nexus.console.auth.api;

import java.util.Optional;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;

/**
 * Looks up login identities. Implemented by platform and kernel; console does not persist users.
 */
public interface UserDirectory {

    /**
     * Finds a user in the given realm by user_name, email, or mobile.
     *
     * @param realm    security realm
     * @param identity login identifier
     * @return matching user when found
     */
    Optional<AuthUser> findByLogin(SecurityRealm realm, String identity);
}
