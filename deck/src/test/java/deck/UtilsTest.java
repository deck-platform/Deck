package deck;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void readProcessOutput() throws IOException {
        String s = "input string\n";
        InputStream is = new ByteArrayInputStream(s.getBytes());
        String out = Utils.readProcessOutput(is);
        assertEquals(s, out);
    }

    @Test
    public void getSubFileListFilter() {
        File file = new File("");
        String filter = ".class";
        List<String> result = Utils.getSubFileListFilter(file, filter, true);
        System.out.println(result);
    }
}