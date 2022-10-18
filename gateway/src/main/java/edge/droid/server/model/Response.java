package edge.droid.server.model;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        return new Response<T>(data);
    }

    public static <T> Response<T> error(CodeMsg codeMsg) {
        return new Response<T>(codeMsg);
    }

    private Response(CodeMsg codeMsg) {
        this.code = codeMsg.getCode();
        this.message = codeMsg.getMessage();
        this.data = null;
    }

    public Response(T data) {
        this.code = CodeMsg.SUCCESS.getCode();
        this.message = CodeMsg.SUCCESS.getMessage();
        this.data = data;
    }
}

