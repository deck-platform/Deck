package edge.droid.server.model;

import edge.droid.server.data.Source;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    private boolean finished;

    private int targetNum;
    private AtomicInteger currentResult;
    private int redundancy = 0;
    // The current dispatch time in FL. In normal task the currentTimes is always 1.
    private int currentTimes = 1;
    private int currentRedundancy;

    public void addRedundancy(int redundancy) {
        this.currentRedundancy = redundancy;
        this.redundancy += redundancy;
    }

    public void incrResult() {
        this.currentResult.incrementAndGet();
    }

    @Data
    private static class LimitFilter {
        private int cpu;
        private int memory;
        private int power;
    }
}
