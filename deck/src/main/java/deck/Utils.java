package deck;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Utils {
    public static String readProcessOutput(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }
        return builder.toString();
    }

    public static boolean cmdExec(String commandStr) {
        log.info("[exec] cmdData={}", commandStr);
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(commandStr);
            int resultCode = p.waitFor();
            if (resultCode == 0) {
                String resp = readProcessOutput(p.getInputStream());
                log.info("[exec] cmdResult={}", resp);
                return true;
            } else {
                String errMsg = readProcessOutput(p.getErrorStream());
                log.error("[exec] cmdErrorResult={}", errMsg);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO: find jar to replace compile way
    // Given a java file and a dir containing all dependencies (jar files), compile to .class file
    public static boolean compile(File file, File libPath, String outputPath) {
        StringBuilder cmdBuilder = new StringBuilder("javac -d ").append(outputPath).append(" ");

        // When libPath==null, jarNameList is empty
        List<String> jarNameList = getSubFileListFilter(libPath,  ".jar", false);
        if (jarNameList.size() != 0) {
            jarNameList.add("."); //make -cp index local dir
            // temporary logic, judge windows or unix
            // todo find a jar to replace cmd
            String os = System.getProperty("os.name").toLowerCase();
            String splitChar = ":";
            if (os.contains("window")) {
                splitChar = ";";
            }
            cmdBuilder.append(" -classpath ").append(String.join(splitChar, jarNameList)).append(" ");
        } else {
            cmdBuilder.append(" ");
        }
        String javaFilePath = file.getAbsolutePath();
        cmdBuilder.append(javaFilePath);
        return Utils.cmdExec(cmdBuilder.toString());
    }

    public static List<String> getSubFileListFilter(File dirFile, String filter, boolean needSubFile) {
        // Case for filtering jar file
        if (null == dirFile && filter.equals(".jar")) {
            log.info("[getSubFileListFilter] no jar file is specified");
            return new ArrayList<>();
        }
        // Case for filtering other files but dirFile is null
        if (null == dirFile) {
            log.error("[getSubFileListFilter] path for filtering {} is null", filter);
            return new ArrayList<>();
        }
        // Case for filtering other files but dirFile is not a dir
        if (!dirFile.isDirectory()) {
            log.error("[getSubFileListFilter] path={} is not a dir", dirFile.getName());
            return new ArrayList<>();
        }
        // Case for normal filtering
        File[] subFileList = dirFile.listFiles();
        if (null == subFileList || subFileList.length == 0) {
            log.info("[getSubFileListFilter] file={} has not subFile", dirFile.getName());
            return new ArrayList<>();
        }
        if (needSubFile) {
            List<String> result = new ArrayList<>();
            try (Stream<Path> walkStream = Files.walk(Paths.get(dirFile.getAbsolutePath()))) {
                walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
                    if (f.toString().endsWith(filter)) {
                        result.add(f.toString());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        } else {
            return Arrays.stream(subFileList)
                    .map(File::getPath)
                    .filter(subFile -> subFile.endsWith(filter))
                    .collect(Collectors.toList());
        }
    }
}
