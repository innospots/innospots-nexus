package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Session/transport snapshot of a user. Contains immutable identity fields
 * ({@code userId}, {@code userName}, {@code realName}) and mutable
 * attributes such as email, avatar, group membership, and assigned roles.
 * Not a kernel domain entity.
 */
public class UserSnapshot {

    public static final String CLAIM_REAL_NAME = "realName";
    public static final String CLAIM_EMAIL = "email";

    private final Long userId;
    private final String userName;
    private final String realName;
    private String email;
    private String avatarKey;
    private LocalDateTime lastAccessTime;
    private BasicStatus status = BasicStatus.ENABLED;
    private UserGroupSnapshot group;
    private final List<RoleSnapshot> roles = new ArrayList<>();

    private UserSnapshot(Long userId, String userName, String realName) {
        this.userId = userId;
        this.userName = userName;
        this.realName = realName;
    }

    public static UserSnapshot simple(Long userId, String userName, String realName) {
        return new UserSnapshot(userId, userName, realName);
    }

    /**
     * Rebuilds a snapshot from the current {@link TLC} identity keys when present.
     */
    public static Optional<UserSnapshot> fromContextOptional() {
        Long userId = TLC.userId();
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(simple(userId, TLC.userName(), null));
    }

    /**
     * Rebuilds a snapshot from the current {@link TLC} identity keys.
     *
     * @throws NexusException when {@link TLC#userId()} is absent
     */
    public static UserSnapshot fromContext() {
        return fromContextOptional()
                .orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
    }

    /**
     * Builds a snapshot from token or session claims. Keys align with {@link TLC}
     * constants ({@link TLC#USER_ID}, {@link TLC#USER_NAME}, etc.).
     */
    public static UserSnapshot fromClaims(Map<String, ?> claims) {
        Checks.notNull(claims, "claims");
        Long userId = parseUserId(claims.get(TLC.USER_ID));
        String userName = stringValue(claims.get(TLC.USER_NAME));
        Checks.notNull(userId, "userId");
        Checks.notBlank(userName, "userName");
        UserSnapshot user = simple(userId, userName, stringValue(claims.get(CLAIM_REAL_NAME)));
        String email = stringValue(claims.get(CLAIM_EMAIL));
        if (email != null) {
            user.email(email);
        }
        return user;
    }

    public Long userId() {
        return userId;
    }

    public String userName() {
        return userName;
    }

    public String realName() {
        return realName;
    }

    public String displayName() {
        return realName == null || realName.isBlank() ? userName : realName;
    }

    public String email() {
        return email;
    }

    public UserSnapshot email(String email) {
        this.email = email;
        return this;
    }

    public String avatarKey() {
        return avatarKey;
    }

    public UserSnapshot avatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
        return this;
    }

    public LocalDateTime lastAccessTime() {
        return lastAccessTime;
    }

    public UserSnapshot lastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
        return this;
    }

    public BasicStatus status() {
        return status;
    }

    public UserSnapshot status(BasicStatus status) {
        this.status = status == null ? BasicStatus.ENABLED : status;
        return this;
    }

    public UserGroupSnapshot group() {
        return group;
    }

    public UserSnapshot group(UserGroupSnapshot group) {
        this.group = group;
        return this;
    }

    public List<RoleSnapshot> roles() {
        return List.copyOf(roles);
    }

    public UserSnapshot role(RoleSnapshot role) {
        if (role != null) {
            roles.add(role);
        }
        return this;
    }

    private static Long parseUserId(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
