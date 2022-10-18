package edge.droid.server.model;

import edge.droid.server.data.Source;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Task {

    private String taskID;
    private String description;
    private String fileDir;
    private String dexPath;
    private String libPath;
    private String resultPath;
    private int limitModelNum;
    private int trainNum;
    private String taskType;
    private LimitFilter limitFilter;
    private Map<Source, List<String>> permissionInfo;

    @Data
    private static class LimitFilter {
        private int cpu;
        private int memory;
        private int power;
    }
}
