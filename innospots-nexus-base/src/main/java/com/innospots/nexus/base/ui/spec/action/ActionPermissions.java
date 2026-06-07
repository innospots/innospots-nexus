package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActionPermissions {

    private List<String> roles = List.of();
    private List<String> permissions = List.of();

    public ActionPermissions() {
    }

    public static ActionPermissions of(List<String> roles, List<String> permissions) {
        ActionPermissions actionPermissions = new ActionPermissions();
        actionPermissions.roles = roles == null ? List.of() : List.copyOf(roles);
        actionPermissions.permissions = permissions == null ? List.of() : List.copyOf(permissions);
        return actionPermissions;
    }

    public List<String> roles() {
        return roles;
    }

    public List<String> permissions() {
        return permissions;
    }
}
