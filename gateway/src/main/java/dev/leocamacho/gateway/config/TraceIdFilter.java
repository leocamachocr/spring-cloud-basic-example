package dev.leocamacho.gateway.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceIdFilter implements GlobalFilter {
    Logger logger = LoggerFactory.getLogger(TraceIdFilter.class);
    @Autowired
    private Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (tracer.currentSpan() != null) {
            String traceId = tracer.currentSpan().context().traceId();
            logger.info("Adding X-B3-TraceId header: {}", traceId);
            exchange.getResponse().getHeaders().add("X-B3-TraceId", traceId);
        } else {
            logger.warn("No current span found, not adding X-B3-TraceId header");
        }
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    Span currentSpan = tracer.currentSpan();
                    if (currentSpan != null) {
                        logger.info("Gateway outgoing request traceId (Micrometer): {}", currentSpan.context().traceId());
                    } else {
                        logger.warn("No current span found in tracer.");
                    }
                });
    }
}
