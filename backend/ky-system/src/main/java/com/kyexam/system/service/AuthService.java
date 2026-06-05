package com.kyexam.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kyexam.common.enums.RoleCode;
import com.kyexam.common.exception.BusinessException;
import com.kyexam.system.dto.AuthUserView;
import com.kyexam.system.dto.LoginRequest;
import com.kyexam.system.dto.LoginResponse;
import com.kyexam.system.entity.SysRole;
import com.kyexam.system.entity.SysUser;
import com.kyexam.system.mapper.SysRoleMapper;
import com.kyexam.system.mapper.SysUserMapper;
import com.kyexam.system.security.CurrentUser;
import com.kyexam.system.security.UserContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthService {
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final AuthTokenService authTokenService;

    public AuthService(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper, AuthTokenService authTokenService) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.authTokenService = authTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        attachRoles(user);
        if (!Boolean.TRUE.equals(user.getEnabled()) || !matchesPassword(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        LoginResponse response = new LoginResponse();
        response.setToken(authTokenService.issue(user.getId()));
        response.setUser(toView(user));
        return response;
    }

    public AuthUserView me() {
        return toView(requireEnabledUser(UserContext.current().getId()));
    }

    public CurrentUser toCurrentUser(SysUser user) {
        Set<RoleCode> roles = resolveRoles(user);
        if (roles.isEmpty()) {
            roles.add(RoleCode.USER);
        }
        return new CurrentUser(user.getId(), user.getUsername(), user.getDisplayName(), roles);
    }

    public SysUser requireEnabledUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "登录用户不存在");
        }
        attachRoles(user);
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException(401, "账号已停用");
        }
        return user;
    }

    public AuthUserView toView(SysUser user) {
        CurrentUser currentUser = toCurrentUser(user);
        AuthUserView view = new AuthUserView();
        view.setId(user.getId());
        view.setUsername(user.getUsername());
        view.setDisplayName(user.getDisplayName());
        view.setRole(currentUser.getRole());
        view.setRoles(new ArrayList<RoleCode>(currentUser.getRoles()));
        return view;
    }

    private void attachRoles(SysUser user) {
        List<SysRole> roles = sysRoleMapper.selectByUserId(user.getId());
        user.setRoles(new LinkedHashSet<SysRole>(roles));
    }

    private static Set<RoleCode> resolveRoles(SysUser user) {
        Set<RoleCode> result = new LinkedHashSet<RoleCode>();
        if (user.getRoles() == null) {
            return result;
        }
        for (SysRole role : user.getRoles()) {
            if (role == null || role.getCode() == null) {
                continue;
            }
            try {
                result.add(RoleCode.valueOf(role.getCode()));
            } catch (IllegalArgumentException ignored) {
                // Ignore unknown role codes in development data.
            }
        }
        return result;
    }

    private static boolean matchesPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }
        if (storedHash.startsWith("{noop}")) {
            return rawPassword.equals(storedHash.substring("{noop}".length()));
        }
        return rawPassword.equals(storedHash);
    }
}
