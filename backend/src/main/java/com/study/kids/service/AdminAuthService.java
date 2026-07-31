package com.study.kids.service;

import com.study.kids.config.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminProperties adminProperties;
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();
    private static final long TOKEN_TTL_MS = 24 * 60 * 60 * 1000L;

    public String login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        if (adminProperties.getUsername().equals(username)
                && adminProperties.getPassword().equals(password)) {
            String token = UUID.randomUUID().toString().replace("-", "");
            tokens.put(token, System.currentTimeMillis() + TOKEN_TTL_MS);
            return token;
        }
        return null;
    }

    public boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Long expireAt = tokens.get(token);
        if (expireAt == null) {
            return false;
        }
        if (expireAt < System.currentTimeMillis()) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public void logout(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
