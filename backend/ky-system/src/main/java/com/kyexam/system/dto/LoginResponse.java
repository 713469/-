package com.kyexam.system.dto;

public class LoginResponse {
    private String token;
    private AuthUserView user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthUserView getUser() {
        return user;
    }

    public void setUser(AuthUserView user) {
        this.user = user;
    }
}
