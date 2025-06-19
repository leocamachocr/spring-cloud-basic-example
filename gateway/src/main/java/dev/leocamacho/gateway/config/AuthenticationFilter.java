package dev.leocamacho.gateway.config;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RefreshScope
public class AuthenticationFilter implements GlobalFilter {
    Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final RouterValidator routerValidator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouterValidator routerValidator, JwtUtil jwtUtil) {
        this.routerValidator = routerValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isAuthenticatedEndpoint.test(request)) {
            if (this.isAuthMissing(request)) {
                return this.onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            final String token = jwtUtil.getToken(this.getAuthHeader(request));

            if (jwtUtil.isInvalid(token)) {
                return this.onError(exchange, HttpStatus.FORBIDDEN);
            }
            ServerWebExchange sessionWebExchange = exchange.mutate()
                    .request(this.buildSessionRequest(exchange, token))
                    .build();
            return chain.filter(sessionWebExchange);
        }
        return chain.filter(exchange);

    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        DataBuffer buffer = response.bufferFactory().wrap("Invalid session".getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }


    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    private ServerHttpRequest buildSessionRequest(ServerWebExchange exchange, String token) {
        Claims claims = jwtUtil.getAllClaimsFromToken(token);

        ServerHttpRequest originalRequest = exchange.getRequest();
        ServerHttpRequest.Builder requestBuilder = originalRequest.mutate();

        // Headers personalizados (JWT claims)
        requestBuilder.header("email", String.valueOf(claims.get("email")));
        requestBuilder.header("id", String.valueOf(claims.get("id")));
        requestBuilder.header("roles", String.valueOf(claims.get("roles")));
        requestBuilder.header("correlationId", UUID.randomUUID().toString());

        // Headers de trazabilidad: preservar si ya existen
        copyTracingHeader(originalRequest, requestBuilder, "X-B3-TraceId");
        copyTracingHeader(originalRequest, requestBuilder, "X-B3-SpanId");
        copyTracingHeader(originalRequest, requestBuilder, "X-B3-ParentSpanId");
        copyTracingHeader(originalRequest, requestBuilder, "X-B3-Sampled");
        copyTracingHeader(originalRequest, requestBuilder, "X-B3-Flags");

        return requestBuilder.build();
    }

    private void copyTracingHeader(ServerHttpRequest original, ServerHttpRequest.Builder builder, String headerName) {
        String value = original.getHeaders().getFirst(headerName);
        if (value != null) {
            builder.header(headerName, value);
        }
    }

}