package com.kyexam.system.security;

import com.kyexam.common.enums.RoleCode;
import com.kyexam.common.exception.BusinessException;

public final class UserContext {
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<CurrentUser>();

    private UserContext() {
    }

    public static CurrentUser current() {
        CurrentUser user = CURRENT.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static void requireRole(RoleCode expected) {
        requireAnyRole(expected);
    }

    public static void requireAnyRole(RoleCode... expectedRoles) {
        CurrentUser user = current();
        if (expectedRoles == null || expectedRoles.length == 0) {
            return;
        }
        for (RoleCode expected : expectedRoles) {
            if (user.hasRole(expected)) {
                return;
            }
        }
        throw new BusinessException(403, "当前角色无权执行该操作");
    }
}
