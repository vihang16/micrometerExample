package org.example.config;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationConvention;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.observation.ServerHttpObservationDocumentation;
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Configuration(proxyBeanMethods = false)
public class MicroMeterConfig {

    private static final Logger logger = Loggers.getLogger(MicroMeterConfig.class);

    @Order(Ordered.HIGHEST_PRECEDENCE)
    WebServerFactoryCustomizer<ReactiveWebServerFactory> observedTomcatWebServerFactoryCustomizer(
            ObservationRegistry observationRegistry) {
        return factory -> factory.getWebServer(new ObservedValve(observationRegistry));

    }
}
class ObservedValve implements HttpHandler {
    private static final ServerRequestObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultServerRequestObservationConvention();
    private final ObservationRegistry observationRegistry;


    public ObservedValve(ObservationRegistry observationRegistry) {
        this.observationRegistry =  observationRegistry;

    }


    @Override
    public Mono<Void> handle(ServerHttpRequest request, ServerHttpResponse response) {
        HttpHeaders headers = request.getHeaders();
        String traceId = "TraceId";
        Map<String, Object> attributes =  new HashMap<>();
        for(String key : headers.keySet())
            attributes.put(key,headers.getFirst(key));

        Observation observation = ServerHttpObservationDocumentation.HTTP_REACTIVE_SERVER_REQUESTS
                .observation(observationRegistry)
                .start();
        attributes.put(traceId, attributes.getOrDefault(traceId, UUID.randomUUID()));
        attributes.put(Observation.class.getName(), observation);
        ServerRequestObservationContext context = new ServerRequestObservationContext(request, response, attributes);
        try (Observation.Scope scope = observation.openScope()) {

        }
        catch (Exception exception) {
            observation.error(exception);
            throw exception;
        }
        finally {
            observation.stop();
        }

        return null;
    }
}

