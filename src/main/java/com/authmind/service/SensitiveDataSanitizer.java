package com.authmind.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class SensitiveDataSanitizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern JWT_PATTERN = Pattern.compile("\\beyJ[a-zA-Z0-9_-]+\\.eyJ[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+\\b");
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9\\-._~+/]+=*\\b");
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("(?i)(JSESSIONID|PHPSESSID|SESSIONID|SID)=\\s*[A-Za-z0-9\\-._~+/]+");
    private static final Pattern CERTIFICATE_PATTERN = Pattern.compile("-----BEGIN CERTIFICATE-----(?:(?!-----END CERTIFICATE-----).)*-----END CERTIFICATE-----", Pattern.DOTALL);

    public String sanitize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String sanitized = input;
        sanitized = EMAIL_PATTERN.matcher(sanitized).replaceAll("[email-redacted]");
        sanitized = JWT_PATTERN.matcher(sanitized).replaceAll("[jwt-redacted]");
        sanitized = BEARER_TOKEN_PATTERN.matcher(sanitized).replaceAll("Bearer [token-redacted]");
        sanitized = SESSION_ID_PATTERN.matcher(sanitized).replaceAll("$1=[session-redacted]");
        sanitized = CERTIFICATE_PATTERN.matcher(sanitized).replaceAll("[certificate-redacted]");

        return sanitized;
    }
}
