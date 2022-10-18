package deck;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Deck extends BaseDroid {

    public static Task constructTask(File file, File libPath, String outputPath, String taskDesc, Object... params)
            throws IOException, InterruptedException {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("description", taskDesc);
        return constructBaseTask(file, libPath, outputPath, requestParams, params);
    }

    public static DataFrame getDataFrame() {
        return new DataFrame(getGatewayURL(), getTaskID());
    }
}
