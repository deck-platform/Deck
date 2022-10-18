package edge.droid.server.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DecompileUtilsTest {

    @Test
    void decompile() {
        String jarPath = "";
        String name = "dextest.class";
        boolean result = DecompileUtils.decompile(jarPath, name);
        Assertions.assertTrue(result);
    }
}