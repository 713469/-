package com.kyexam.system.service;

import com.kyexam.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthTokenService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${ky.auth.token-secret:ky-examination-dev-secret}")
    private String tokenSecret;

    @Value("${ky.auth.token-ttl-seconds:86400}")
    private long tokenTtlSeconds;

    public String issue(Long userId) {
        long expiresAt = Instant.now().plusSeconds(tokenTtlSeconds).getEpochSecond();
        String payload = userId + ":" + expiresAt + ":" + UUID.randomUUID().toString().replace("-", "");
        return base64Url(payload) + "." + sign(payload);
    }

    public Long parseUserId(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(401, "请先登录");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new BusinessException(401, "登录状态无效");
        }
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(401, "登录状态无效");
        }
        if (!constantTimeEquals(sign(payload), parts[1])) {
            throw new BusinessException(401, "登录状态无效");
        }
        String[] fields = payload.split(":");
        if (fields.length != 3) {
            throw new BusinessException(401, "登录状态无效");
        }
        long expiresAt;
        try {
            expiresAt = Long.parseLong(fields[1]);
        } catch (NumberFormatException exception) {
            throw new BusinessException(401, "登录状态无效");
        }
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new BusinessException(401, "登录已过期");
        }
        try {
            return Long.valueOf(fields[0]);
        } catch (NumberFormatException exception) {
            throw new BusinessException(401, "登录状态无效");
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return base64Url(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new BusinessException(500, "登录签名失败");
        }
    }

    private static String base64Url(String value) {
        return base64Url(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static boolean constantTimeEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
