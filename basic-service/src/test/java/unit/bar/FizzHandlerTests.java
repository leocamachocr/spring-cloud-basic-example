package unit.bar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import dev.leocamacho.basic.bar.FizzHandler;
import dev.leocamacho.basic.foo.BazzHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FizzHandlerTests {

    private BazzHandler bazzHandler;

    private FizzHandler fizzHandler;

    @BeforeEach
    void setup() {
        bazzHandler = mock(BazzHandler.class);
        fizzHandler = new FizzHandler(bazzHandler);
    }


    @Test
    void testPassParametersToBazz() {
        ArgumentCaptor<BazzHandler.Command> bazzCommandCaptor = forClass(BazzHandler.Command.class);
        when(bazzHandler.handler(bazzCommandCaptor.capture())).thenReturn("it works");

        FizzHandler.Command command = new FizzHandler.Command("input");
        assertThat(fizzHandler.handle(command)).isEqualTo("it works");


    }
}
