package org.example.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.Optional;
import java.util.UUID;

public class TraceIdFilter implements WebFilter {

    public static final String TRACE_ID_HEADER_NAME = "TraceId";
    private static final String STATUS_URI_PATTERN = "_status";

    private Timer timer;
    private static final Logger logger = Loggers.getLogger(TraceIdFilter.class);

    private final CustomTraceBaggage customTraceBaggage;

    public TraceIdFilter(MeterRegistry meterRegistry, CustomTraceBaggage customTraceBaggage) {
        this.customTraceBaggage = customTraceBaggage;
        this.timer = Timer.builder("trace_id_filter_timer")
                .description("Timer for Trace ID Filter")
                .register(meterRegistry);
    }



    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain chain) {
        Timer.Sample sample = Timer.start();
        return Mono.just(serverWebExchange)
                .doOnNext(exchange -> {
                    final ServerHttpRequest request = exchange.getRequest();
                    final String uriPath = request.getURI().getPath();
                    final String incomingTraceId = request.getHeaders().getFirst(TRACE_ID_HEADER_NAME);
                    final String traceId = Optional.ofNullable(incomingTraceId).orElse(UUID.randomUUID().toString());
                    customTraceBaggage.updateTraceId(traceId);
                    MDC.put(TRACE_ID_HEADER_NAME, traceId);
                    logConditionally(uriPath, "step=request-started, MSG=Request, incomingTraceId={}, requestURI={}, HttpMethod={}",
                            incomingTraceId,
                            uriPath,
                            request.getMethod());
                })
                .doOnNext(exchange -> exchange.getResponse()
                        .beforeCommit(() -> {
                            exchange
                                    .getResponse()
                                    .getHeaders()
                                    .add(TRACE_ID_HEADER_NAME, customTraceBaggage.getTraceId());

                            final ServerHttpRequest request = exchange.getRequest();
                            final String requestUri = request.getURI().getPath();
                            long duration = sample.stop(timer);
                            logConditionally(requestUri, "step=request-completed, MSG=Response, incomingTraceId={}, requestURI={}, HttpMethod={}, responseStatus={}, content-length={}, timeTaken={}",
                                    (Object) request.getHeaders().getFirst(TRACE_ID_HEADER_NAME),
                                    requestUri,
                                    request.getMethod(),
                                    exchange.getResponse().getStatusCode(),
                                    exchange.getResponse().getHeaders().getFirst("content-length"), duration);
                            return Mono.empty();
                        })
                )
                .then(chain.filter(serverWebExchange));
    }
    private void logConditionally(final String requestUri, final String format, final Object... arguments) {
        if (!requestUri.contains(STATUS_URI_PATTERN)) {
            logger.info(format, arguments);
        }
    }
}
