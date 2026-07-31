package com.study.kids.controller;

import com.study.kids.common.ApiResponse;
import com.study.kids.service.AdminAuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = adminAuthService.login(request.getUsername(), request.getPassword());
        if (token == null) {
            return ApiResponse.fail("账号或密码错误");
        }
        return ApiResponse.ok(Map.of("token", token));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "X-Admin-Token", required = false) String token) {
        adminAuthService.logout(token);
        return ApiResponse.ok();
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
