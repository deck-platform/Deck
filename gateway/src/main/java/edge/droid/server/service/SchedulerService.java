package edge.droid.server.service;

import edge.droid.server.model.DelayTask;
import edge.droid.server.model.Task;

import java.util.concurrent.TimeUnit;

public interface SchedulerService {

    boolean addScheduler(Task task, long time, TimeUnit unit, int times);

    boolean addScheduler(DelayTask delayTask);

    void start();
}
