package com.authmind.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedAnalyzerTest {

    private RuleBasedAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new RuleBasedAnalyzer();
    }

    @Test
    void analyze_nullInput_returnsEmpty() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(null, null, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void analyze_blankInput_returnsEmpty() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze("", "", "", "");
        assertTrue(result.isEmpty());
    }

    @Test
    void analyze_appNotConfiguredForUser_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: app_not_configured_for_user",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("app_not_configured_for_user", result.get().rule());
        assertEquals("User is not assigned to application", result.get().likelyCause());
        assertEquals(95, result.get().confidence());
    }

    @Test
    void analyze_appNotConfiguredForUser_caseInsensitive_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: APP_NOT_CONFIGURED_FOR_USER",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("app_not_configured_for_user", result.get().rule());
    }

    @Test
    void analyze_invalidIssuer_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: invalid_issuer in SAML response",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("invalid_issuer", result.get().rule());
        assertEquals("Issuer mismatch between IdP and SP configuration", result.get().likelyCause());
        assertEquals(90, result.get().confidence());
    }

    @Test
    void analyze_signatureValidationFailed_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: signature validation failed",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("signature_validation_failed", result.get().rule());
        assertEquals("Signing certificate mismatch or expired certificate", result.get().likelyCause());
        assertEquals(88, result.get().confidence());
    }

    @Test
    void analyze_signatureValidationFailed_underscoreVariant_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: signature_validation_failed",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("signature_validation_failed", result.get().rule());
    }

    @Test
    void analyze_audienceMismatch_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: audience mismatch",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("audience_mismatch", result.get().rule());
        assertEquals("Audience URI does not match expected value", result.get().likelyCause());
        assertEquals(85, result.get().confidence());
    }

    @Test
    void analyze_audienceMismatch_underscoreVariant_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: audience_mismatch",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("audience_mismatch", result.get().rule());
    }

    @Test
    void analyze_invalidAudience_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: invalid audience",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("audience_mismatch", result.get().rule());
    }

    @Test
    void analyze_relayState_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: relaystate configuration issue",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("relaystate", result.get().rule());
        assertEquals("RelayState configuration problem", result.get().likelyCause());
        assertEquals(80, result.get().confidence());
    }

    @Test
    void analyze_relayState_underscoreVariant_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: relay_state issue",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("relaystate", result.get().rule());
    }

    @Test
    void analyze_nameId_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: nameid format issue",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("nameid", result.get().rule());
        assertEquals("NameID format or mapping issue", result.get().likelyCause());
        assertEquals(78, result.get().confidence());
    }

    @Test
    void analyze_nameId_underscoreVariant_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: name_id mapping issue",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("nameid", result.get().rule());
    }

    @Test
    void analyze_ruleInOktaLog_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Generic error",
                null,
                "Okta log shows app_not_configured_for_user",
                null
        );

        assertTrue(result.isPresent());
        assertEquals("app_not_configured_for_user", result.get().rule());
    }

    @Test
    void analyze_ruleInSamlTrace_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                null,
                "SAML response contains invalid_issuer",
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("invalid_issuer", result.get().rule());
    }

    @Test
    void analyze_ruleInOidcError_detectsRule() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                null,
                null,
                null,
                "OIDC error: audience mismatch"
        );

        assertTrue(result.isPresent());
        assertEquals("audience_mismatch", result.get().rule());
    }

    @Test
    void analyze_noKnownRule_returnsEmpty() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Generic error message with no known pattern",
                null,
                null,
                null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void analyze_multipleRules_returnsFirstMatch() {
        Optional<RuleBasedAnalyzer.RuleMatch> result = analyzer.analyze(
                "Error: app_not_configured_for_user and also invalid_issuer",
                null,
                null,
                null
        );

        assertTrue(result.isPresent());
        assertEquals("app_not_configured_for_user", result.get().rule());
    }
}
