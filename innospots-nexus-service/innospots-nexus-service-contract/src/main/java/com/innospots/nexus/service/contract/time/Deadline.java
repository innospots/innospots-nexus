package com.innospots.nexus.service.contract.time;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;

/**
 * 剩余执行边界。剩余时间永不为负。{@link #shorten(Duration)} 不会延长当前截止时间。
 *
 * @author Smars
 * @date 2026/09/13
 * @see Ticker
 */
public interface Deadline {

    /**
     * 返回剩余时间，永不为负。
     *
     * @return 剩余时长
     */
    Duration remaining();

    /**
     * 返回截止时间是否已过。
     *
     * @return 已过期时为 {@code true}
     */
    boolean isExpired();

    /**
     * 返回此截止时间是否无有限边界。
     *
     * @return 无限制时为 {@code true}
     */
    boolean isUnlimited();

    /**
     * 返回不晚于从现在起 {@code timeout} 后过期的截止时间。
     *
     * @param timeout 正数超时
     * @return 缩短后的截止时间
     */
    Deadline shorten(Duration timeout);

    /**
     * 返回无有限边界的截止时间。
     *
     * @return 无限制截止时间
     */
    static Deadline unlimited() {
        return UnlimitedDeadline.INSTANCE;
    }

    /**
     * 返回基于 {@code ticker} 度量的有限截止时间。
     *
     * @param ticker  单调时钟源
     * @param timeout 自当前 ticker 读数的正数超时
     * @return 有限截止时间
     */
    static Deadline of(Ticker ticker, Duration timeout) {
        Checks.notNull(ticker, "ticker");
        Checks.notNull(timeout, "timeout");
        Checks.isTrue(!timeout.isZero() && !timeout.isNegative(), "timeout must be positive");
        return new FiniteDeadline(ticker, timeout);
    }
}

final class UnlimitedDeadline implements Deadline {

    static final UnlimitedDeadline INSTANCE = new UnlimitedDeadline();

    private UnlimitedDeadline() {
    }

    @Override
    public Duration remaining() {
        return Duration.ofNanos(Long.MAX_VALUE);
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public boolean isUnlimited() {
        return true;
    }

    @Override
    public Deadline shorten(Duration timeout) {
        return Deadline.of(Ticker.system(), timeout);
    }
}

final class FiniteDeadline implements Deadline {

    private final Ticker ticker;
    private final long deadlineNanos;

    FiniteDeadline(Ticker ticker, Duration timeout) {
        this.ticker = ticker;
        long now = ticker.readNanos();
        long sum = now + timeout.toNanos();
        this.deadlineNanos = sum < now ? Long.MAX_VALUE : sum;
    }

    @Override
    public Duration remaining() {
        long left = deadlineNanos - ticker.readNanos();
        if (left <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(left);
    }

    @Override
    public boolean isExpired() {
        return ticker.readNanos() >= deadlineNanos;
    }

    @Override
    public boolean isUnlimited() {
        return false;
    }

    @Override
    public Deadline shorten(Duration timeout) {
        Checks.notNull(timeout, "timeout");
        Checks.isTrue(!timeout.isZero() && !timeout.isNegative(), "timeout must be positive");
        if (remaining().compareTo(timeout) <= 0) {
            return this;
        }
        return Deadline.of(ticker, timeout);
    }
}
