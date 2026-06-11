package com.authmind.controller;

import com.authmind.model.SsoAnalysisRequest;
import com.authmind.model.SsoAnalysisResponse;
import com.authmind.service.SsoAnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SsoAnalysisController {

    private final SsoAnalysisService ssoAnalysisService;

    public SsoAnalysisController(SsoAnalysisService ssoAnalysisService) {
        this.ssoAnalysisService = ssoAnalysisService;
    }

    @PostMapping("/analyze")
    public SsoAnalysisResponse analyze(@Valid @RequestBody SsoAnalysisRequest request) {
        return ssoAnalysisService.analyze(request);
    }
}
