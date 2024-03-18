package dev.leocamacho.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    public static final List<String> anonymousEndpoints = List.of(
            "/api/public"
    );
    public static final List<String> authenticatedEndpoints = List.of(
            "/api/private"
    );

    public Predicate<ServerHttpRequest> isAuthenticatedEndpoint =
            request -> authenticatedEndpoints
                    .stream()
                    .anyMatch(uri ->
                            request.getURI().getPath().startsWith(uri)
                    );

    public Predicate<ServerHttpRequest> isAnonymous =
            request -> anonymousEndpoints
                    .stream()
                    .anyMatch(uri -> request.getURI().getPath().startsWith(uri));

}
