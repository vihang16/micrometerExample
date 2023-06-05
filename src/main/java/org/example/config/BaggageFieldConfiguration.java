package org.example.config;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaggageFieldConfiguration {

    private static final String TRACE_ID = "traceId";

    @Bean
    BaggageField traceIdField() {
        return BaggageField.create(TRACE_ID);
    }

    @Bean
    CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
        return MDCScopeDecorator.newBuilder()
                .clear()
                .add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(traceIdField())
                        .flushOnUpdate()
                        .build())
                .build();
    }
}
