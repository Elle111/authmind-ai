package com.authmind.service.identity;

import com.authmind.model.IdentityProviderType;
import com.authmind.model.SsoAnalysisRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdentityProviderDetectorTest {

    private IdentityProviderDetector detector;

    @BeforeEach
    void setUp() {
        detector = new IdentityProviderDetector();
    }

    @Test
    void detect_appNotConfiguredForUser_returnsOkta() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error: app_not_configured_for_user",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.OKTA, detector.detect(request));
    }

    @Test
    void detect_aadsts50011_returnsEntraId() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error: AADSTS50011",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.ENTRA_ID, detector.detect(request));
    }

    @Test
    void detect_loginMicrosoftOnline_returnsEntraId() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error at login.microsoftonline.com",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.ENTRA_ID, detector.detect(request));
    }

    @Test
    void detect_pingfederate_returnsPingIdentity() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error from pingfederate",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.PING_IDENTITY, detector.detect(request));
    }

    @Test
    void detect_keycloak_returnsKeycloak() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error from keycloak",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.KEYCLOAK, detector.detect(request));
    }

    @Test
    void detect_auth0DotCom_returnsAuth0() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error at auth0.com",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.AUTH0, detector.detect(request));
    }

    @Test
    void detect_adfs_returnsADFS() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error from adfs",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.ADFS, detector.detect(request));
    }

    @Test
    void detect_accountsGoogleDotCom_returnsGoogleWorkspace() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error at accounts.google.com",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.GOOGLE_WORKSPACE, detector.detect(request));
    }

    @Test
    void detect_samlResponse_returnsGenericSaml() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error with SAMLResponse",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.GENERIC_SAML, detector.detect(request));
    }

    @Test
    void detect_idToken_returnsGenericOidc() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error with id_token",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.GENERIC_OIDC, detector.detect(request));
    }

    @Test
    void detect_unknownText_returnsUnknown() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Generic error with no known pattern",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.UNKNOWN, detector.detect(request));
    }

    @Test
    void detect_nullInput_returnsUnknown() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                null,
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.UNKNOWN, detector.detect(request));
    }

    @Test
    void detect_blankInput_returnsUnknown() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "",
                "",
                "",
                ""
        );

        assertEquals(IdentityProviderType.UNKNOWN, detector.detect(request));
    }

    @Test
    void detect_caseInsensitive_okta() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Error: OKTA",
                null,
                null,
                null
        );

        assertEquals(IdentityProviderType.OKTA, detector.detect(request));
    }

    @Test
    void detect_inIdentityProviderLog() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                "Generic error",
                null,
                "Log shows keycloak",
                null
        );

        assertEquals(IdentityProviderType.KEYCLOAK, detector.detect(request));
    }

    @Test
    void detect_inSamlTrace() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                null,
                "SAML trace with auth0.com",
                null,
                null
        );

        assertEquals(IdentityProviderType.AUTH0, detector.detect(request));
    }

    @Test
    void detect_inOidcError() {
        SsoAnalysisRequest request = new SsoAnalysisRequest(
                null,
                null,
                null,
                "OIDC error with AADSTS"
        );

        assertEquals(IdentityProviderType.ENTRA_ID, detector.detect(request));
    }
}
