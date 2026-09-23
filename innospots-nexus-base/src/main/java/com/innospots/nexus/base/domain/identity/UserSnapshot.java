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
 * 用户的会话/传输快照。包含不可变身份字段（{@code userId}、{@code userName}、{@code realName}）及可变属性如邮箱、头像、组成员与分配角色。非 kernel 领域实体。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.thread.SessionContext
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
     * 当 {@link TLC} 中存在身份键时，从当前线程上下文重建快照。
     *
     * @return 快照可选值，无用户 ID 时为空
     */
    public static Optional<UserSnapshot> fromContextOptional() {
        Long userId = TLC.userId();
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(simple(userId, TLC.userName(), null));
    }

    /**
     * 从当前 {@link TLC} 身份键重建快照。
     *
     * @return 用户快照
     * @throws NexusException 当 {@link TLC#userId()} 缺失时
     */
    public static UserSnapshot fromContext() {
        return fromContextOptional()
                .orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
    }

    /**
     * 从令牌或会话声明构建快照。键与 {@link TLC} 常量
     * （{@link TLC#USER_ID}、{@link TLC#USER_NAME} 等）对齐。
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
