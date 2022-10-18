package edge.droid.server.utils;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.*;
import com.thoughtworks.qdox.model.expression.AnnotationValue;
import edge.droid.server.data.Source;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JavaAnalysisUtils {

    private static JavaProjectBuilder builder = new JavaProjectBuilder();

    public static List<String> getImports(File file) throws IOException {
        JavaSource javaSource = builder.addSource(file);
        return javaSource.getImports();
    }

    public static Map<Source, List<String>> getAnnotationMap(List<File> fileList) throws IOException {
        Map<Source, List<String>> res = new HashMap<>();
        for (File file : fileList) {
            JavaSource javaSource = builder.addSource(file);
            List<JavaClass> javaClassList = javaSource.getClasses();
            for (JavaClass javaClass : javaClassList) {
                List<JavaMethod> javaMethodList = javaClass.getMethods();
                for (JavaMethod javaMethod : javaMethodList) {
                    List<JavaAnnotation> javaAnnotationList = javaMethod.getAnnotations();
                    for (JavaAnnotation javaAnnotation : javaAnnotationList) {
                        Source source = Source.getByDescription(javaAnnotation.getType().getName());
                        String value = javaAnnotation.getProperty("value").toString();
                        if (!res.containsKey(source)) {
                            res.put(source, new ArrayList<>());
                        }
                        res.get(source).add(value.substring(1, value.length() - 1));
                    }
                }
            }
        }
        return res;
    }
}
