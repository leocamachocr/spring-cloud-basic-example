package dev.leocamacho.authentication.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static dev.leocamacho.authentication.session.SessionContextHolder.getSession;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor sessionRequestInterceptor() {
        return new SessionRequestInterceptor();
    }
}

class SessionRequestInterceptor implements RequestInterceptor {

    private static final String ID_HEADER = "id";
    private static final String EMAIL_HEADER = "email";
    private static final String ROLES_HEADER = "roles";
    private static final String CORRELATION_ID_HEADER = "correlationId";

    @Override
    public void apply(RequestTemplate template) {

        template.header(ID_HEADER, getSession().getId());
        template.header(EMAIL_HEADER, getSession().email());
        template.header(ROLES_HEADER, getSession().getRoles());
        template.header(CORRELATION_ID_HEADER, getSession().correlationId().toString());
    }
}