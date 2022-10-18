package edge.droid.server.utils;

import lombok.extern.slf4j.Slf4j;
import org.benf.cfr.reader.api.CfrDriver;

import java.util.*;

@Slf4j
public class DecompileUtils {

    public static boolean decompile(String classPath, String className) {
        Map<String, String> options = new HashMap<>();
        options.put("outputdir", classPath);
        try {
            CfrDriver driver = new CfrDriver.Builder().withOptions(options).build();
            driver.analyse(Collections.singletonList(classPath + className));
            return true;
        } catch (Exception e) {
            log.error("[decompile] classPath={}, className={}", classPath, className, e);
            return false;
        }
    }
}
