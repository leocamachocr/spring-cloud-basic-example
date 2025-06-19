package dev.leocamacho.authentication.http;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceIdResponseFilter extends OncePerRequestFilter {
    Logger logger = LoggerFactory.getLogger(TraceIdResponseFilter.class);
    @Autowired
    private Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws java.io.IOException, jakarta.servlet.ServletException {
        if (tracer.currentSpan() != null) {
            logger.info("Adding X-B3-TraceId header: {}", tracer.currentSpan().context().traceId());
            response.addHeader("X-B3-TraceId", tracer.currentSpan().context().traceId());
        }else{
            logger.warn("No current span found, not adding X-B3-TraceId header");
        }
        filterChain.doFilter(request, response);
    }
}