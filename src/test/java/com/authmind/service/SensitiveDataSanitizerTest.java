package com.authmind.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SensitiveDataSanitizerTest {

    private SensitiveDataSanitizer sanitizer;

    @BeforeEach
    void setUp() {
        sanitizer = new SensitiveDataSanitizer();
    }

    @Test
    void sanitize_nullInput_returnsNull() {
        assertNull(sanitizer.sanitize(null));
    }

    @Test
    void sanitize_blankInput_returnsBlank() {
        assertEquals("", sanitizer.sanitize(""));
        assertEquals("   ", sanitizer.sanitize("   "));
    }

    @Test
    void sanitize_email_masksEmail() {
        String input = "Contact john.smith@company.com for support";
        String result = sanitizer.sanitize(input);
        assertEquals("Contact [email-redacted] for support", result);
    }

    @Test
    void sanitize_multipleEmails_masksAll() {
        String input = "Email user1@test.com and user2@test.org";
        String result = sanitizer.sanitize(input);
        assertEquals("Email [email-redacted] and [email-redacted]", result);
    }

    @Test
    void sanitize_jwt_masksJwt() {
        String input = "Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String result = sanitizer.sanitize(input);
        assertEquals("Token: [jwt-redacted]", result);
    }

    @Test
    void sanitize_bearerToken_masksToken() {
        String input = "Authorization: Bearer abc123xyz";
        String result = sanitizer.sanitize(input);
        assertEquals("Authorization: Bearer [token-redacted]", result);
    }

    @Test
    void sanitize_bearerTokenCaseInsensitive_masksToken() {
        String input = "Authorization: bearer ABC123XYZ";
        String result = sanitizer.sanitize(input);
        assertEquals("Authorization: Bearer [token-redacted]", result);
    }

    @Test
    void sanitize_sessionId_masksSessionId() {
        String input = "Cookie: JSESSIONID=ABC123XYZ";
        String result = sanitizer.sanitize(input);
        assertEquals("Cookie: JSESSIONID=[session-redacted]", result);
    }

    @Test
    void sanitize_sessionIdCaseInsensitive_masksSessionId() {
        String input = "Cookie: jsessionid=ABC123XYZ";
        String result = sanitizer.sanitize(input);
        assertEquals("Cookie: jsessionid=[session-redacted]", result);
    }

    @Test
    void sanitize_certificate_masksCertificate() {
        String input = "-----BEGIN CERTIFICATE-----\nMIIDXTCCAkWgAwIBAgIJAKL0UG+mRKN7MA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV\n-----END CERTIFICATE-----";
        String result = sanitizer.sanitize(input);
        assertEquals("[certificate-redacted]", result);
    }

    @Test
    void sanitize_multipleSensitiveData_masksAll() {
        String input = "Email: john@test.com, Token: Bearer abc123, Session: JSESSIONID=XYZ123";
        String result = sanitizer.sanitize(input);
        assertEquals("Email: [email-redacted], Token: Bearer [token-redacted], Session: JSESSIONID=[session-redacted]", result);
    }

    @Test
    void sanitize_noSensitiveData_returnsOriginal() {
        String input = "This is a normal error message without sensitive data";
        String result = sanitizer.sanitize(input);
        assertEquals(input, result);
    }
}
