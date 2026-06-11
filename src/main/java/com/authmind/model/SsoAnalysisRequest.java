package com.authmind.model;

import jakarta.validation.constraints.NotBlank;

public record SsoAnalysisRequest(
        @NotBlank(message = "errorMessage is required")
        String errorMessage,

        String samlTrace,

        String oktaLog,

        String oidcError
) {}
