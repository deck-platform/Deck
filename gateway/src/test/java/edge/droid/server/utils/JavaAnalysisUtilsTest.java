package edge.droid.server.utils;

import edge.droid.server.data.Source;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JavaAnalysisUtilsTest {

    @Test
    public void testGetImports() throws IOException {
        File file = new File("");
        List<String> result = JavaAnalysisUtils.getImports(file);
        System.out.println(result);
    }

    @Test
    public void testGetAnnotation() throws IOException {
        String curDir = Paths.get("").toAbsolutePath().toString();
        File file = new File(curDir + "/resources/dextest.java");
        List<File> fileList = new ArrayList<>();
        fileList.add(file);
        Map<Source, List<String>> res = JavaAnalysisUtils.getAnnotationMap(fileList);
        for (Map.Entry<Source, List<String>> entry : res.entrySet()) {
            System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
        }

    }
}
