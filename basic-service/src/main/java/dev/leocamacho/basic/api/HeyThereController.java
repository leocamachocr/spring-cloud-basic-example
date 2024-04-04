package dev.leocamacho.basic.api;

import com.netflix.discovery.EurekaClient;
import dev.leocamacho.basic.session.SessionContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private/basic")
public class HeyThereController {
    Logger logger = LoggerFactory.getLogger(HeyThereController.class);
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;


    @Value("${spring.application.name}")
    private String appName;

    @GetMapping
    public String greeting() {
        logger.info("Session: {}", SessionContextHolder.getSession().correlationId());
        return String.format("Hello from '%s'!", eurekaClient.getApplication(appName).getName());
    }
}
