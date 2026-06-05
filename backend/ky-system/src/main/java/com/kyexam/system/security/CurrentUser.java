package com.kyexam.system.security;

import com.kyexam.common.enums.RoleCode;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class CurrentUser {
    private Long id;
    private String username;
    private String displayName;
    private RoleCode role;
    private Set<RoleCode> roles = new LinkedHashSet<RoleCode>();

    public CurrentUser() {
    }

    public CurrentUser(Long id, String username, RoleCode role) {
        this(id, username, username, Collections.singleton(role));
    }

    public CurrentUser(Long id, String username, String displayName, Set<RoleCode> roles) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        setRoles(roles);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public RoleCode getRole() {
        return role;
    }

    public void setRole(RoleCode role) {
        this.role = role;
        this.roles = new LinkedHashSet<RoleCode>();
        if (role != null) {
            this.roles.add(role);
        }
    }

    public Set<RoleCode> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleCode> roles) {
        this.roles = roles == null ? new LinkedHashSet<RoleCode>() : new LinkedHashSet<RoleCode>(roles);
        this.role = resolvePrimaryRole(this.roles);
    }

    public boolean hasRole(RoleCode role) {
        return roles != null && roles.contains(role);
    }

    private static RoleCode resolvePrimaryRole(Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        if (roles.contains(RoleCode.SYSTEM_ADMIN)) {
            return RoleCode.SYSTEM_ADMIN;
        }
        if (roles.contains(RoleCode.COMMUNITY_ADMIN)) {
            return RoleCode.COMMUNITY_ADMIN;
        }
        return RoleCode.USER;
    }
}
