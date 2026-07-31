package com.study.kids.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class AdminStartupLogger implements ApplicationRunner {

    private final AdminProperties adminProperties;

    @Override
    public void run(ApplicationArguments args) {
        String pwd = adminProperties.getPassword() == null ? "" : adminProperties.getPassword();
        String masked = pwd.isEmpty()
                ? "(empty)"
                : "*".repeat(Math.max(0, pwd.length() - 2)) + pwd.substring(Math.max(0, pwd.length() - 2));
        log.info("管理端账号已加载: username={}, passwordMasked={}", adminProperties.getUsername(), masked);
    }
}
