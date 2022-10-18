package edge.droid.server.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CodeMsg<T>{
    private int code;
    private String message;

    public final static CodeMsg SUCCESS = new CodeMsg(200, "success");
}