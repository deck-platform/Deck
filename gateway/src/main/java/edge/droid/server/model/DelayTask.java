package edge.droid.server.model;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public class DelayTask implements Delayed {

    private long time;
    private Task task;
    private int times;

    public DelayTask(Task task, long time, TimeUnit unit, int times) {
        this.task = task;
        this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
        this.times = times;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return time - System.currentTimeMillis();
    }
    @Override
    public int compareTo(Delayed o) {
        DelayTask delayTask = (DelayTask) o;
        long diff = this.time - delayTask.time;
        if (diff <= 0) {
            return -1;
        } else {
            return 1;
        }
    }
}
