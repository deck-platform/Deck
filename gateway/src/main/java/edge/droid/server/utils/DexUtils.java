package edge.droid.server.utils;

import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;

@Slf4j
public class DexUtils {

    public static boolean generateDexByD8(Path outputDir, List<Path> fileList) {
        try {
            log.info("[generateDexByD8] generate dex outputDir={}, fileList={}", outputDir, fileList);
            D8.run(D8Command.builder()
                    .addProgramFiles(fileList)
                    .setOutput(outputDir, OutputMode.DexIndexed)
                    .build());
            return true;
        } catch (CompilationFailedException e) {
            log.error("[generateDexByD8] generate dex error", e);
            return false;
        }
    }
}
