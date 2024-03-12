package dev.leocamacho.basic.session;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SessionFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        var id = httpRequest.getHeader("id");
        var email = httpRequest.getHeader("email");
        var roles = httpRequest.getHeader("roles");
        var correlationId = httpRequest.getHeader("correlationId");
        SessionContextHolder.setSession(Session.newBuilder()
                .withId(id)
                .withCorrelationId(correlationId)
                .withEmail(email)
                .withRoles(roles)
                .build());

        filterChain.doFilter(servletRequest, servletResponse);

        SessionContextHolder.clearSession();
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
