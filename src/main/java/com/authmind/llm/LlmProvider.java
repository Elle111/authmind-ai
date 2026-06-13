package com.authmind.llm;

import com.authmind.model.SsoAnalysisResponse;

public interface LlmProvider {

    SsoAnalysisResponse analyze(String prompt);
}
