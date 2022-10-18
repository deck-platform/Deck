package edge.droid.server.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.MDC;

public class NanoTimeAppender extends ConsoleAppender<ILoggingEvent> {

    @Override
    public void stop() {
        MDC.clear();
        super.stop();
    }

    @Override
    public void append(ILoggingEvent event) {
        if (event == null || !isStarted()){
            return;
        }
        MDC.put("time", String.valueOf(System.nanoTime()));
        super.append(event);
    }
}
