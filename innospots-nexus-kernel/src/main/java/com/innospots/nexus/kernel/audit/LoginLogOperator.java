package com.innospots.nexus.kernel.audit;

import java.util.List;

/**
 * Port for login audit recording and query operations.
 */
public interface LoginLogOperator {

    /**
     * Records a login audit entry.
     *
     * @param loginLog login audit entry
     * @return recorded audit entry
     */
    LoginLog record(LoginLog loginLog);

    /**
     * Lists recent login audit entries for a user.
     *
     * @param userId user identifier
     * @param limit  maximum number of entries
     * @return recent login audit entries
     */
    List<LoginLog> listRecent(String userId, int limit);
}
