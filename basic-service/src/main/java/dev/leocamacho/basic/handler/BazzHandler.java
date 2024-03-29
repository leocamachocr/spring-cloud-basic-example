package dev.leocamacho.basic.handler;

import org.springframework.stereotype.Component;

@Component
public class BazzHandler {

    public record Command(String name) {
    }

    public String handler(Command command) {
        return command.name().replaceAll(" ", "_");
    }
}
