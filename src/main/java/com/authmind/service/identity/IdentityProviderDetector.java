package com.authmind.service.identity;

import com.authmind.model.IdentityProviderType;
import com.authmind.model.SsoAnalysisRequest;
import org.springframework.stereotype.Component;

@Component
public class IdentityProviderDetector {

    public IdentityProviderType detect(SsoAnalysisRequest request) {
        String combinedInput = combineInputs(
                request.errorMessage(),
                request.samlTrace(),
                request.identityProviderLog(),
                request.oidcError()
        );

        if (combinedInput == null || combinedInput.isBlank()) {
            return IdentityProviderType.UNKNOWN;
        }

        String lowerInput = combinedInput.toLowerCase();

        // OKTA
        if (lowerInput.contains("okta") ||
            lowerInput.contains("app_not_configured_for_user") ||
            lowerInput.contains("idx") ||
            lowerInput.contains("okta.com")) {
            return IdentityProviderType.OKTA;
        }

        // ENTRA_ID
        if (lowerInput.contains("aadsts") ||
            lowerInput.contains("login.microsoftonline.com") ||
            lowerInput.contains("sts.windows.net") ||
            lowerInput.contains("microsoft entra") ||
            lowerInput.contains("azure ad")) {
            return IdentityProviderType.ENTRA_ID;
        }

        // PING_IDENTITY
        if (lowerInput.contains("pingfederate") ||
            lowerInput.contains("pingone") ||
            lowerInput.contains("pingidentity")) {
            return IdentityProviderType.PING_IDENTITY;
        }

        // KEYCLOAK
        if (lowerInput.contains("keycloak") ||
            lowerInput.contains("kc_idp_hint") ||
            lowerInput.contains("realms/")) {
            return IdentityProviderType.KEYCLOAK;
        }

        // AUTH0
        if (lowerInput.contains("auth0") ||
            lowerInput.contains("auth0.com")) {
            return IdentityProviderType.AUTH0;
        }

        // ADFS
        if (lowerInput.contains("adfs") ||
            lowerInput.contains("active directory federation services")) {
            return IdentityProviderType.ADFS;
        }

        // GOOGLE_WORKSPACE
        if (lowerInput.contains("accounts.google.com") ||
            lowerInput.contains("google workspace") ||
            lowerInput.contains("google.com/a/")) {
            return IdentityProviderType.GOOGLE_WORKSPACE;
        }

        // GENERIC_SAML
        if (lowerInput.contains("samlresponse") ||
            lowerInput.contains("<saml") ||
            lowerInput.contains("assertion") ||
            lowerInput.contains("nameid") ||
            lowerInput.contains("audience") ||
            lowerInput.contains("issuer")) {
            return IdentityProviderType.GENERIC_SAML;
        }

        // GENERIC_OIDC
        if (lowerInput.contains("openid") ||
            lowerInput.contains("id_token") ||
            lowerInput.contains("access_token") ||
            lowerInput.contains("redirect_uri") ||
            lowerInput.contains("client_id")) {
            return IdentityProviderType.GENERIC_OIDC;
        }

        return IdentityProviderType.UNKNOWN;
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
