package deck;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class GenericClassTest {

    static class Result<T> {
        private T content;

        public void set(T t) {
            content = t;
        }

        public T get() {
            return content;
        }

        // Constructor
        Result(T t) {
            content = t;
        }

        Result(){}
    }

    static class Model {
        public String modelPath;
        public String modelName;
        public Byte[] modelBytes;

        // Constructor
        Model(String modelPath, String modelName) {
            this.modelPath = modelPath;
            this.modelName = modelName;
        }

        Model(){}
    }

    public static <T> Result<T> run(T... param) {
        System.out.println(param.length);
        System.out.println(param[0]);
        System.out.println(param[1]);
        return new Result<>(param[0]);
    }

    public static Result<Model> getModel(Model model) {
        return new Result<>(model);
    }

    @Test
    public void testRun() {
        Model model = new Model("path", "name");
        Result<Model> ret = getModel(model);
        System.out.println(ret.content.modelName + ret.content.modelPath);
    }

    @Test
    public void serialize() throws Exception {
        Model mnnModel = new Model("modelPath", "mnnModel");
        Result<Model> result = new Result<Model>(mnnModel);
        System.out.println(result.get().modelName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Kryo kryo = new Kryo();
        kryo.register(Model.class);
        kryo.register(Result.class);
        kryo.register(Byte[].class);
        Output output = new Output(baos);
        kryo.writeObject(output, result);
        output.close();

        System.out.println(baos.size());
        System.out.println(baos.toString());

    }
}
