package edge.droid.server.service;

import edge.droid.server.data.AuthorityResult;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CheckServiceTest {

    @Autowired
    private CheckerService checkerService;

    @Test
    public void checkCodeSecurity() {
        AuthorityResult authorityResult1 = checkerService.checkCodeSecurity(new File("C:\\我的内容\\桌面\\code\\code-package\\example\\reflect"));
        Assertions.assertNotEquals(authorityResult1, AuthorityResult.SUCCESS);

        AuthorityResult authorityResult2 = checkerService.checkCodeSecurity(new File("C:\\我的内容\\桌面\\code\\code-package\\example\\picture"));
        Assertions.assertEquals(authorityResult2, AuthorityResult.SUCCESS);
    }

    @Test
    public void checkDexSecurity() throws IOException {
        AuthorityResult authorityResult1 = checkerService.checkDexSecurity(new File("C:\\我的内容\\桌面\\classes.dex"));
        Assertions.assertNotEquals(authorityResult1, AuthorityResult.SUCCESS);
    }

    @Test
    public void testSootInsert() {
        //checkerService.sootInsert("C:\\我的内容\\桌面\\dex\\raw_classes.dex");
        //checkerService.sootInsert("C:\\我的内容\\桌面\\dex\\test\\json.dex");
        //checkerService.sootInsert("C:\\我的内容\\桌面\\dex\\test\\gson.dex");
        checkerService.sootInsert("C:\\我的内容\\桌面\\dex\\test\\http.dex", "");
        //checkerService.sootInsert("C:\\我的内容\\桌面\\dex\\kube_classes.dex");
//        checkerService.sootInsert("C:\\我的内容\\桌面\\dex\\classes.dex");
    }
}
