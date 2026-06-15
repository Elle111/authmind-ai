package com.authmind.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RuleBasedAnalyzer {

    public record RuleMatch(String rule, String likelyCause, int confidence) {}

    public Optional<RuleMatch> analyze(String errorMessage, String samlTrace, String identityProviderLog, String oidcError) {
        String combinedInput = combineInputs(errorMessage, samlTrace, identityProviderLog, oidcError);

        if (combinedInput == null || combinedInput.isBlank()) {
            return Optional.empty();
        }

        if (combinedInput.toLowerCase().contains("app_not_configured_for_user")) {
            return Optional.of(new RuleMatch(
                "app_not_configured_for_user",
                "User is not assigned to application",
                95
            ));
        }

        if (combinedInput.toLowerCase().contains("aadsts50011")) {
            return Optional.of(new RuleMatch(
                "redirect_uri_mismatch",
                "Redirect URI mismatch",
                90
            ));
        }

        if (combinedInput.toLowerCase().contains("invalid_issuer")) {
            return Optional.of(new RuleMatch(
                "invalid_issuer",
                "Issuer mismatch between IdP and SP configuration",
                90
            ));
        }

        if (combinedInput.toLowerCase().contains("signature validation failed") ||
            combinedInput.toLowerCase().contains("signature_validation_failed")) {
            return Optional.of(new RuleMatch(
                "signature_validation_failed",
                "Signing certificate mismatch or expired certificate",
                88
            ));
        }

        if (combinedInput.toLowerCase().contains("audience mismatch") ||
            combinedInput.toLowerCase().contains("audience_mismatch") ||
            combinedInput.toLowerCase().contains("invalid audience")) {
            return Optional.of(new RuleMatch(
                "audience_mismatch",
                "Audience URI does not match expected value",
                85
            ));
        }

        if (combinedInput.toLowerCase().contains("relaystate") ||
            combinedInput.toLowerCase().contains("relay_state")) {
            return Optional.of(new RuleMatch(
                "relaystate",
                "RelayState configuration problem",
                80
            ));
        }

        if (combinedInput.toLowerCase().contains("nameid") ||
            combinedInput.toLowerCase().contains("name_id")) {
            return Optional.of(new RuleMatch(
                "nameid",
                "NameID format or mapping issue",
                78
            ));
        }

        return Optional.empty();
    }

    private String combineInputs(String errorMessage, String samlTrace, String identityProviderLog, String oidcError) {
        StringBuilder combined = new StringBuilder();

        if (errorMessage != null && !errorMessage.isBlank()) {
            combined.append(errorMessage).append(" ");
        }
        if (samlTrace != null && !samlTrace.isBlank()) {
            combined.append(samlTrace).append(" ");
        }
        if (identityProviderLog != null && !identityProviderLog.isBlank()) {
            combined.append(identityProviderLog).append(" ");
        }
        if (oidcError != null && !oidcError.isBlank()) {
            combined.append(oidcError).append(" ");
        }

        return combined.toString().trim();
    }
}
