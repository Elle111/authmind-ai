package com.authmind.controller;

import com.authmind.model.SsoAnalysisRequest;
import com.authmind.model.SsoAnalysisResponse;
import com.authmind.service.SsoAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SsoAnalysisController.class)
class SsoAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SsoAnalysisService ssoAnalysisService;

    @Test
    void analyze_validRequest_returnsOk() throws Exception {
        SsoAnalysisResponse mockResponse = new SsoAnalysisResponse(
                "Test cause",
                85,
                "Test explanation",
                List.of("Step 1", "Step 2"),
                List.of("Check 1"),
                List.of("Note 1"),
                "test_rule",
                "Confidence explanation"
        );

        when(ssoAnalysisService.analyze(any(SsoAnalysisRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"errorMessage\":\"Test error\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likelyCause").value("Test cause"))
                .andExpect(jsonPath("$.confidenceScore").value(85))
                .andExpect(jsonPath("$.ruleDetected").value("test_rule"));
    }

    @Test
    void analyze_missingErrorMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyze_blankErrorMessage_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"errorMessage\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyze_withOptionalFields_returnsOk() throws Exception {
        SsoAnalysisResponse mockResponse = new SsoAnalysisResponse(
                "Test cause",
                85,
                "Test explanation",
                List.of("Step 1"),
                List.of("Check 1"),
                List.of("Note 1"),
                null,
                null
        );

        when(ssoAnalysisService.analyze(any(SsoAnalysisRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"errorMessage\":\"Test error\",\"samlTrace\":\"SAML data\",\"oktaLog\":\"Okta log\",\"oidcError\":\"OIDC error\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likelyCause").exists());
    }
}
