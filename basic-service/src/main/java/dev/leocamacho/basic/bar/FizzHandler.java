package dev.leocamacho.basic.bar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import dev.leocamacho.basic.foo.BazzHandler;

@Controller
public class FizzHandler {

    private BazzHandler bazz;

    @Autowired
    public FizzHandler(BazzHandler bazz) {
        this.bazz = bazz;
    }

    public record Command(String text) {
        public BazzHandler.Command toBazzCommand() {
            return new BazzHandler.Command(this.text);
        }
    }


    public String handle(Command command) {
        return bazz.handler(command.toBazzCommand());

    }
}

