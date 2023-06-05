package org.example.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.util.Logger;
import reactor.util.Loggers;

@Configuration(proxyBeanMethods = false)
public class MicroMeterConfig {

    private static final Logger logger = Loggers.getLogger(MicroMeterConfig.class);

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }

    @Bean
    public MeterRegistry meterRegistry() {
        MeterRegistry meterRegistry = Metrics.globalRegistry;
        logger.info("MDC get value:{}", MDC.get("TraceId"));
        meterRegistry.config().commonTags("TraceId", MDC.get("TraceId"));
        return meterRegistry;
    }
}
