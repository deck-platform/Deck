package edge.droid.server.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompileUtilsTest {

    @Test
    void compile() {
        String libPath = "";
        String name = "";
        boolean result = CompileUtils.compile(name, libPath);
        Assertions.assertTrue(result);
    }
}
