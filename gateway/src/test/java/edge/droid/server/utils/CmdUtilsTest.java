package edge.droid.server.utils;

import org.junit.jupiter.api.Test;

class CmdUtilsTest {

    @Test
    public void testExec() {
        CmdUtils.exec("ls");
    }
}
