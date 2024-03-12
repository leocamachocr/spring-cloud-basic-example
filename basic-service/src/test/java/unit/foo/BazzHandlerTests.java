package unit.foo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import dev.leocamacho.basic.foo.BazzHandler;

import static org.assertj.core.api.Assertions.*;


public class BazzHandlerTests {



    @Test
    void testAWordWithoutSpaces(){
        Assertions.assertThat(new BazzHandler()
                .handler(new BazzHandler.Command("hola")))
                .isEqualTo("hola");
    }
        @Test
    void testAWordWithSpaces(){
        assertThat(new BazzHandler()
                .handler(new BazzHandler.Command(" hola mundo ")))
                .isEqualTo("_hola_mundo_");
    }

}
