package com.innospots.nexus.console.credential.password.policy;

/**
 * 登录密码失败后的锁定策略。
 *
 * @param maxFailedAttempts 连续失败达到该次数后写入 {@code lockedUntil}
 * @param lockDurationMinutes 锁定时长（分钟）
 */
public record LoginLockPolicy(int maxFailedAttempts, int lockDurationMinutes) {

    /**
     * 默认：5 次失败锁定 15 分钟。
     */
    public static final LoginLockPolicy DEFAULT = new LoginLockPolicy(5, 15);

    public LoginLockPolicy {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("maxFailedAttempts must be at least 1");
        }
        if (lockDurationMinutes < 1) {
            throw new IllegalArgumentException("lockDurationMinutes must be at least 1");
        }
    }
}
