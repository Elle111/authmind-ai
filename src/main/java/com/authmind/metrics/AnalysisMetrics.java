package com.authmind.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AnalysisMetrics {

    private final Counter requestCounter;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter ruleMatchCounter;

    public AnalysisMetrics(MeterRegistry meterRegistry) {
        this.requestCounter = Counter.builder("authmind.analysis.requests")
                .description("Total number of SSO analysis requests")
                .register(meterRegistry);

        this.successCounter = Counter.builder("authmind.analysis.success")
                .description("Total number of successful SSO analysis requests")
                .register(meterRegistry);

        this.failureCounter = Counter.builder("authmind.analysis.failures")
                .description("Total number of failed SSO analysis requests")
                .register(meterRegistry);

        this.ruleMatchCounter = Counter.builder("authmind.rule.matches")
                .description("Total number of rule-based detections")
                .register(meterRegistry);
    }

    public void incrementRequest() {
        requestCounter.increment();
    }

    public void incrementSuccess() {
        successCounter.increment();
    }

    public void incrementFailure() {
        failureCounter.increment();
    }

    public void incrementRuleMatch() {
        ruleMatchCounter.increment();
    }
}
