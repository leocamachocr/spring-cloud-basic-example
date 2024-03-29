package dev.leocamacho.authentication.api;

import dev.leocamacho.authentication.client.basic.HeyThereClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@RestController
@RequestMapping("/api/private/auth/hey")
public class HeyThereController {

    @Autowired
    private HeyThereClient heyThereClient;

    @GetMapping("/client")
    public String greeting() {
        return format("I am auth, this is my friend %s",heyThereClient.greeting());
    }

    @GetMapping("/admin")
    public String admin() {
        return heyThereClient.greeting();
    }

}
