package edge.droid.server.aop;

import edge.droid.server.service.TimeLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class TimeLogAspect {

    // now must has "taskID" in params

    @Autowired
    private TimeLogService timeLogService;

    @Pointcut("@annotation(edge.droid.server.aop.TimeLogAop)")
    public void annotationCallTime() {
    }

    @Around(value = "annotationCallTime()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String taskID = getTaskID(joinPoint);
        if (Strings.isBlank(taskID)) {
            log.warn("[TimeLogAspect] params no taskID");
            return joinPoint.proceed();
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        TimeLogAop timeLogAop = method.getAnnotation(TimeLogAop.class);
        timeLogService.setTimeLog(timeLogAop.type(), taskID, timeLogAop.desc() + "Begin", String.valueOf(System.currentTimeMillis()));
        Object result = joinPoint.proceed();
        timeLogService.setTimeLog(timeLogAop.type(), taskID, timeLogAop.desc() + "End", String.valueOf(System.currentTimeMillis()));
        return result;
    }

    private String getTaskID(ProceedingJoinPoint joinPoint) {
        Object[] values = joinPoint.getArgs();
        String[] names = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        for (int index=0; index <= names.length; index+=1) {
            if (names[index].equals("taskID")) {
                return String.valueOf(values[index]);
            }
        }
        return "";
    }
}
