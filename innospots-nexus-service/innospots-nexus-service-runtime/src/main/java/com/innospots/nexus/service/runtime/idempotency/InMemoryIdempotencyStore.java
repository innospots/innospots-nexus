package com.innospots.nexus.service.runtime.idempotency;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.innospots.nexus.base.util.Checks;

/**
 * 单 JVM 幂等存储。
 */
public final class InMemoryIdempotencyStore {

    /**
     * 准入决策。
     */
    public enum AdmissionDecision {
        ADMITTED,
        IN_FLIGHT,
        CONFLICT
    }

    private record StoredEntry(String fingerprint, IdempotencyState state, Instant expiresAt) {
    }

    private final int maxEntries;
    private final Duration ttl;
    private final Map<IdempotencyKey, StoredEntry> entries = new ConcurrentHashMap<>();

    /**
     * 创建存储。
     *
     * @param maxEntries 最大条目数
     * @param ttl        生存时间
     */
    public InMemoryIdempotencyStore(int maxEntries, Duration ttl) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.maxEntries = maxEntries;
        this.ttl = Checks.notNull(ttl, "ttl");
    }

    /**
     * 尝试准入幂等键。
     *
     * @param key         幂等键
     * @param fingerprint 请求指纹
     * @return 准入决策
     */
    public synchronized AdmissionDecision admit(IdempotencyKey key, String fingerprint) {
        Checks.notNull(key, "key");
        Checks.notBlank(fingerprint, "fingerprint");
        evictExpired();
        StoredEntry existing = entries.get(key);
        Instant expiresAt = Instant.now().plus(ttl);
        if (existing == null) {
            ensureCapacity();
            entries.put(key, new StoredEntry(fingerprint, IdempotencyState.IN_FLIGHT, expiresAt));
            return AdmissionDecision.ADMITTED;
        }
        if (!existing.fingerprint().equals(fingerprint)) {
            return AdmissionDecision.CONFLICT;
        }
        if (existing.state() == IdempotencyState.IN_FLIGHT || existing.state() == IdempotencyState.UNKNOWN) {
            return AdmissionDecision.IN_FLIGHT;
        }
        entries.put(key, new StoredEntry(fingerprint, IdempotencyState.IN_FLIGHT, expiresAt));
        return AdmissionDecision.ADMITTED;
    }

    /**
     * 标记终态。
     *
     * @param key   幂等键
     * @param state 终态
     */
    public synchronized void complete(IdempotencyKey key, IdempotencyState state) {
        Checks.notNull(key, "key");
        Checks.notNull(state, "state");
        StoredEntry existing = entries.get(key);
        if (existing == null) {
            return;
        }
        entries.put(key, new StoredEntry(existing.fingerprint(), state, Instant.now().plus(ttl)));
    }

    private void ensureCapacity() {
        if (entries.size() < maxEntries) {
            return;
        }
        evictExpired();
        if (entries.size() >= maxEntries) {
            Iterator<IdempotencyKey> iterator = entries.keySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    private void evictExpired() {
        Instant now = Instant.now();
        entries.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }
}
