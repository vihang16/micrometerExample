package org.example.config;

import brave.baggage.BaggageField;

import org.springframework.stereotype.Component;

@Component
public class CustomTraceBaggage {

    private final BaggageField traceIdValue;

    public CustomTraceBaggage(BaggageField baggageField){
        traceIdValue = baggageField;
    }

    public boolean updateTraceId(final String value) {
        return traceIdValue.updateValue(value);
    }

    public String getTraceId() {
        return traceIdValue.getValue();
    }
}
