package edge.droid.server.service;

import edge.droid.server.data.AuthorityResult;
import edge.droid.server.data.Source;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CheckerService {

    AuthorityResult checkCodeSecurity(File dir);

    // TODO implement check detail
    AuthorityResult checkDexSecurity(File file) throws IOException;

    AuthorityResult checkPermissionSecurity(Map<Source, List<String>> sourceListMap);

    boolean sootInsert(String dexFilePath, String taskDirPath);
}
