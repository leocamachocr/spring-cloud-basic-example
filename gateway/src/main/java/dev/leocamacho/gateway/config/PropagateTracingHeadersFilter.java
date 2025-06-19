package dev.leocamacho.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PropagateTracingHeadersFilter implements GlobalFilter {

    private static final List<String> TRACE_HEADERS = List.of(
            "X-B3-TraceId", "X-B3-SpanId", "X-B3-ParentSpanId", "X-B3-Sampled", "X-B3-Flags"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();

        for (String header : TRACE_HEADERS) {
            String value = exchange.getRequest().getHeaders().getFirst(header);
            if (value != null) {
                requestBuilder.header(header, value);
            }
        }

        return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
    }
}