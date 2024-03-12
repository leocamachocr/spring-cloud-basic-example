package dev.leocamacho.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BasicServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BasicServiceApplication.class);
        application.setAddCommandLineProperties(false);
        application.run(args);
    }

}
