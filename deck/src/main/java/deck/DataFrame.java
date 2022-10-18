package deck;

import com.alibaba.fastjson.JSON;
import com.github.kevinsawicki.http.HttpRequest;
import deck.data.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class DataFrame {

    // maybe not need
    private String uri;
    private String taskID;

    private List<String> aggregateColumnList;
    private String operationType;
    private String operationColumn;
    private Object otherParam;

    public DataFrame(String uri, String taskID) {
        this.taskID = taskID;
        this.uri = uri;
    }

    public Map<String, Object> get(int limit, int waitSeconds) {
        if (null == this.operationType || "".equals(this.operationType)) {
            throw new RuntimeException("operationType can not be blank");
        }
        if (null == this.operationColumn || "".equals(this.operationColumn)) {
            throw new RuntimeException("operationColumn can not be blank");
        }
        if (null == this.aggregateColumnList || aggregateColumnList.isEmpty()) {
            throw new RuntimeException("aggregateColumnList can not be blank");
        }
        StringBuilder stringBuilder = new StringBuilder().append("http://").append(this.uri).append("/task/aggregate?");
        log.info("aggregateColumnList={}", aggregateColumnList);
        for (String aggregateColumn : this.aggregateColumnList) {
            stringBuilder.append("aggregate_column=").append(aggregateColumn);
        }
        String uri = stringBuilder.toString();
        HttpRequest httpRequest = HttpRequest.get(uri, true, "task_id", this.taskID, "wait_seconds", waitSeconds,
                "limit", limit, "operation_type", this.operationType, "operation_column", this.operationColumn, "other_param", this.otherParam);
        log.info("uri={}", httpRequest.toString());
        if (!httpRequest.ok()) {
            log.error("[get] request error, code+{}", httpRequest.code());
            throw new RuntimeException("request error");
        }
        Response<Map<String, Object>> response = JSON.parseObject(httpRequest.body(), Response.class);
        //GlobalData.timeMap.put("getEnd", System.currentTimeMillis());
        log.info("[get] response code:{}, message:{}", response.getCode(), response.getMessage());
        //sendTimeLog(taskId);
        return response.getData();
    }

    /**
     * Set aggregateColumnList.
     * Support multi call.
     * Must call at least once.
     */
    public DataFrame groupBy(String ... aggregateColumn) {
        if (null == this.aggregateColumnList) {
            this.aggregateColumnList = Arrays.asList(aggregateColumn);
        } else {
            this.aggregateColumnList.addAll(Arrays.asList(aggregateColumn));
        }
        return this;
    }

    /**
     * Set operationType and operationColumn.
     * Don't support multi call, the last one will overwrite the first one.
     */
    public DataFrame sum(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "sum";
        return this;
    }

    public DataFrame count(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "count";
        return this;
    }

    public DataFrame avg(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "avg";
        return this;
    }

    public DataFrame unique(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "unique";
        return this;
    }

    public DataFrame collapse(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "collapse";
        return this;
    }

    public DataFrame mean(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "mean";
        return this;
    }

    public DataFrame percentile(String operationColumn, double quantile) {
        // todo other params
        this.operationColumn = operationColumn;
        this.operationType = "percentile";
        this.otherParam = quantile;
        return this;
    }

    public DataFrame prod(String operationColumn) {
        this.operationColumn = operationColumn;
        this.operationType = "prod";
        return this;
    }


}
