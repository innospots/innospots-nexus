package com.innospots.nexus.service.contract.time;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;

/**
 * Remaining execution bound. Remaining time is never negative. {@link #shorten(Duration)} never
 * extends the current deadline.
 *
 * @author Smars
 * @date 2026/09/13
 * @see Ticker
 */
public interface Deadline {

    /**
     * Returns remaining time, never negative.
     *
     * @return remaining duration
     */
    Duration remaining();

    /**
     * Returns whether the deadline has already passed.
     *
     * @return {@code true} when expired
     */
    boolean isExpired();

    /**
     * Returns whether this deadline has no finite bound.
     *
     * @return {@code true} when unlimited
     */
    boolean isUnlimited();

    /**
     * Returns a deadline that expires no later than {@code timeout} from now.
     *
     * @param timeout positive timeout
     * @return shortened deadline
     */
    Deadline shorten(Duration timeout);

    /**
     * Returns a deadline with no finite bound.
     *
     * @return unlimited deadline
     */
    static Deadline unlimited() {
        return UnlimitedDeadline.INSTANCE;
    }

    /**
     * Returns a finite deadline measured against {@code ticker}.
     *
     * @param ticker  monotonic source
     * @param timeout positive timeout from the current ticker reading
     * @return finite deadline
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
