package dev.leocamacho.authentication.session;

import dev.leocamacho.authentication.session.Session;
import dev.leocamacho.authentication.session.SessionContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SessionFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        Session session = Session.newBuilder()
                .withId(httpRequest.getHeader("id"))
                .withCorrelationId(httpRequest.getHeader("correlationId"))
                .withEmail(httpRequest.getHeader("email"))
                .withRoles(httpRequest.getHeader("roles"))
                .build();
        SessionContextHolder.setSession(session);
        MDC.put("correlationId", session.correlationId().toString());

        ((HttpServletResponse) servletResponse).setHeader("correlationId", session.correlationId().toString());
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove("correlationId");
            SessionContextHolder.clearSession();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
