package dev.leocamacho.basic;

import com.netflix.discovery.EurekaClient;
import dev.leocamacho.basic.session.SessionContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private/basic")
public class HeyThereController {
    @Autowired
    @Lazy
    private EurekaClient eurekaClient;

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping
    public String greeting() {
        System.out.println(SessionContextHolder.getSession());
        return String.format("Hello from '%s'!", eurekaClient.getApplication(appName).getName());
    }
}
