package com.kyexam.system.security;

import com.kyexam.common.exception.BusinessException;
import com.kyexam.system.entity.SysUser;
import com.kyexam.system.service.AuthService;
import com.kyexam.system.service.AuthTokenService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class HeaderUserFilter extends OncePerRequestFilter {
    private final AuthTokenService authTokenService;
    private final AuthService authService;

    public HeaderUserFilter(AuthTokenService authTokenService, AuthService authService) {
        this.authTokenService = authTokenService;
        this.authService = authService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || "/api/auth/login".equals(path)
                || "/api/health".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = resolveBearerToken(request);
            Long userId = authTokenService.parseUserId(token);
            SysUser user = authService.requireEnabledUser(userId);
            UserContext.set(authService.toCurrentUser(user));
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            writeError(response, exception.getCode(), exception.getMessage());
        } finally {
            UserContext.clear();
        }
    }

    private static String resolveBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private static void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + escapeJson(message) + "\",\"data\":null}");
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
