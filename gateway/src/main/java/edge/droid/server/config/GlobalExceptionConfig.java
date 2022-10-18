package edge.droid.server.config;

import edge.droid.server.model.CodeMsg;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class GlobalExceptionConfig {

    @ExceptionHandler(value = Exception.class)
    public CodeMsg UserExceptionHandler(HttpServletResponse response, Exception e) {
        CodeMsg codeMsg = new CodeMsg(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        e.printStackTrace();
        return codeMsg;
    }
}