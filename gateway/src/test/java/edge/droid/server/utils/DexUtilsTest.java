package edge.droid.server.utils;

import com.android.tools.r8.CompilationFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


class DexUtilsTest {

    @Test
    void generateDexByD8() {
        Path outputDir = Paths.get("").toAbsolutePath();
        List<Path> classList = new ArrayList<>();
        classList.add(Paths.get(outputDir.toString() + "/dextest.class"));
        boolean result = DexUtils.generateDexByD8(outputDir, classList);
        Assertions.assertTrue(result);
    }
}