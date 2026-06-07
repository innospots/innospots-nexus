package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core user identity model. Contains immutable identity fields
 * ({@code userId}, {@code userName}, {@code realName}) and mutable
 * attributes such as email, avatar, group membership, and assigned roles.
 */
public class UserInfo {

    private final Long userId;
    private final String userName;
    private final String realName;
    private String email;
    private String avatarKey;
    private LocalDateTime lastAccessTime;
    private BasicStatus status = BasicStatus.ENABLED;
    private UserGroupInfo group;
    private final List<RoleInfo> roles = new ArrayList<>();

    private UserInfo(Long userId, String userName, String realName) {
        this.userId = userId;
        this.userName = userName;
        this.realName = realName;
    }

    public static UserInfo simple(Long userId, String userName, String realName) {
        return new UserInfo(userId, userName, realName);
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

    public UserInfo email(String email) {
        this.email = email;
        return this;
    }

    public String avatarKey() {
        return avatarKey;
    }

    public UserInfo avatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
        return this;
    }

    public LocalDateTime lastAccessTime() {
        return lastAccessTime;
    }

    public UserInfo lastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
        return this;
    }

    public BasicStatus status() {
        return status;
    }

    public UserInfo status(BasicStatus status) {
        this.status = status == null ? BasicStatus.ENABLED : status;
        return this;
    }

    public UserGroupInfo group() {
        return group;
    }

    public UserInfo group(UserGroupInfo group) {
        this.group = group;
        return this;
    }

    public List<RoleInfo> roles() {
        return List.copyOf(roles);
    }

    public UserInfo role(RoleInfo role) {
        if (role != null) {
            roles.add(role);
        }
        return this;
    }
}
