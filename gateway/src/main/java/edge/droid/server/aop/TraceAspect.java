package edge.droid.server.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import java.util.UUID;

@Order(1)
@Aspect
@Component
public class TraceAspect {

    private static final String TRACE_ID = "TRACE_ID";

    @Pointcut("execution(* edge.droid.server.websocket.WebSocketHandler..*.*(..))")
    public void pointCut() {}

    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        MDC.put(TRACE_ID, UUID.randomUUID().toString());
    }

    @After("pointCut()")
    public void after(JoinPoint joinPoint){
        MDC.remove(TRACE_ID);
    }
}