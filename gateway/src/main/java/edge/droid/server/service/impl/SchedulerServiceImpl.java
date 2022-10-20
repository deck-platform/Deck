package edge.droid.server.service.impl;

import edge.droid.server.model.DelayTask;
import edge.droid.server.model.Task;
import edge.droid.server.service.SchedulerService;
import edge.droid.server.service.TaskManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerServiceImpl implements SchedulerService {

    @Autowired
    private TaskManagerService taskManagerService;

    private DelayQueue<DelayTask> delayQueue = new DelayQueue<>();
    @Value("${fl.base.model}")
    private String FLBaseModelPath;


    @Override
    public boolean addScheduler(Task task, long time, TimeUnit unit, int times) {
        DelayTask delayTask = new DelayTask(task, time, unit, times);
        return addScheduler(delayTask);
    }

    @Override
    public boolean addScheduler(DelayTask delayTask) {
        return delayQueue.add(delayTask);
    }

    @Override
    public void start() {
        while (true) {
            while (!delayQueue.isEmpty()) {
                DelayTask delayTask = delayQueue.poll();
                Task task = delayTask.getTask();

                // judge task finished or times finished
                if (task.getTaskType().equals("FL")) {
                    if (task.getTrainNum() == task.getCurrentTimes() || task.getCurrentTimes() > delayTask.getTimes() ) {
                        continue;
                    }
                } else {
                    if (task.getCurrentResult().get() >= task.getTargetNum()) {
                        continue;
                    }
                }

                // Compute the new dispatch number based on the historical data.
                int redundancy = computeRedundancy(task);
                task.addRedundancy(redundancy);
                taskManagerService.tranTask(task, FLBaseModelPath, task.getCurrentTimes());
                this.addScheduler(delayTask);
            }
        }
    }

    public int computeRedundancy(Task task) {
        // Todo: add redundancy compute
        // todo: log history uuid to filter
        return 0;
    }
}
