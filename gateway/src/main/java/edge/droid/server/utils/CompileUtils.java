package edge.droid.server.utils;

import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Slf4j
public class CompileUtils {

    public static boolean compile(String sourceFilePath, String libFilePath) {
        // todo get file by dir
        List<File> libFileList = Arrays.asList(new File(libFilePath).listFiles());
        List<File> sourceFileList = Arrays.asList(new File(sourceFilePath).listFiles());
        List<File> outputDir= Arrays.asList(new File(sourceFilePath)); // same with source
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        try {
            StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(
                    null, null, null);
            fileManager.setLocation(StandardLocation.CLASS_PATH, libFileList);
            fileManager.setLocation(StandardLocation.SOURCE_PATH, sourceFileList);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, outputDir);

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFileList);
            Iterable<String> options = Arrays.asList("-source","1.8");
            JavaCompiler.CompilationTask task = javaCompiler
                    .getTask(null, fileManager, null, options,null, compilationUnits);
            return task.call();
        } catch (Exception e) {
            log.error("[compile] compile error", e);
            return false;
        }
    }
}
