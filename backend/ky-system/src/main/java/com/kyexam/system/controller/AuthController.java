package com.kyexam.system.controller;

import com.kyexam.common.api.ApiResponse;
import com.kyexam.system.dto.AuthUserView;
import com.kyexam.system.dto.LoginRequest;
import com.kyexam.system.dto.LoginResponse;
import com.kyexam.system.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserView> me() {
        return ApiResponse.ok(authService.me());
    }
}
