package com.kyexam.system.dto;

import com.kyexam.common.enums.RoleCode;

import java.util.ArrayList;
import java.util.List;

public class AuthUserView {
    private Long id;
    private String username;
    private String displayName;
    private RoleCode role;
    private List<RoleCode> roles = new ArrayList<RoleCode>();

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
    }

    public List<RoleCode> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleCode> roles) {
        this.roles = roles;
    }
}
