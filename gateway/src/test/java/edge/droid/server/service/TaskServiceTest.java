package edge.droid.server.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    void createTask() {
    }

    @Test
    void runTask() {
    }

    @Test
    void getTask() throws InterruptedException {
        String taskID = "test";
        int limit = 3;
        int waitSecond = 30;
        List<String> result = taskService.getTask(taskID, limit, waitSecond);
        System.out.println(result);
    }

    @Test
    void aggregateResult() throws Exception {
        String taskID = "test";
        int limit = 3;
        int waitSecond = 30;
        Map<String, Object>  test = taskService.aggregateResult(null, null, null, null, null, null, null);
        System.out.println(test);
    }
}