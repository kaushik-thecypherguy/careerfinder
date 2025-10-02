package com.acf.careerfinder.admin;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
public class AdminGuard {

    @Value("${admin.secret:}")
    private String adminSecret;

    public void check(String provided) {
        if (adminSecret == null || adminSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Admin secret not configured");
        }
        if (!Objects.equals(adminSecret, provided)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bad admin secret");
        }
    }
}