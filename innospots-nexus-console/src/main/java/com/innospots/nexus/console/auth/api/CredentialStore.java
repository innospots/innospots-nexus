package com.innospots.nexus.console.auth.api;

import java.util.Optional;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;

/**
 * Reads and updates password credentials per realm.
 */
public interface CredentialStore {

    /**
     * Loads the password credential for a realm user.
     *
     * @param realm  security realm
     * @param userId platform or tenant user id
     * @return credential when present
     */
    Optional<CredentialRecord> findPassword(SecurityRealm realm, String userId);

    /**
     * Persists credential updates such as failed attempts or a new hash.
     *
     * @param realm      security realm
     * @param credential updated credential
     */
    void updatePassword(SecurityRealm realm, CredentialRecord credential);
}
