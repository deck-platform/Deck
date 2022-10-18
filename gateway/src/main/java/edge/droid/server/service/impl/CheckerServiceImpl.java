package edge.droid.server.service.impl;

import edge.droid.server.data.AuthorityResult;
import edge.droid.server.data.GlobalData;
import edge.droid.server.data.Source;
import edge.droid.server.service.CheckerService;
import edge.droid.server.utils.JavaAnalysisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.G;
import soot.Value;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CheckerServiceImpl implements CheckerService {

    @org.springframework.beans.factory.annotation.Value("${soot.android.jar.path}")
    private String sootAndroidJarPath;
    @org.springframework.beans.factory.annotation.Value("${soot.classpath}")
    private String sootClasspath;

    private static final String SOOT_BLACK_CLASS = "java.lang.Class";
    private static final String SOOT_BLACK_METHOD = "forName";
    private static final String SOOT_INSERT_CLASS = "deck.Check";
    private static final String SOOT_INSERT_METHOD_DESC = "void codeCheck(java.lang.String)";


    @Override
    public AuthorityResult checkCodeSecurity(File dir) {
        if (!dir.isDirectory()) {
            log.error("[checkCodeSecurity] dir={} is not a dir", dir.getPath());
            return AuthorityResult.UNKNOWN_ERROR;
        }
        List<File> javaFileList = Arrays.stream(dir.listFiles())
                .filter(file -> file.getPath().endsWith(".java"))
                .collect(Collectors.toList());
        if (javaFileList.size() == 0) {
            log.error("[checkCodeSecurity] dir={} has not java file", dir.getPath());
            return AuthorityResult.UNKNOWN_ERROR;
        }
        return check(javaFileList);
    }

    @Override
    public AuthorityResult checkDexSecurity(File file) throws IOException {
        DexFile dexFile = DexFileFactory.loadDexFile(file, null); //todo check why null
        for (ClassDef classDef: dexFile.getClasses()) {
            log.info(String.valueOf(classDef.getMethods()));
//            for (Method method : classDef.getDirectMethods()) {
//                method.
//            }
        }
        return null;
    }

    @Override
    public AuthorityResult checkPermissionSecurity(Map<Source, List<String>> sourceListMap) {
        // Todo add user permission check
        return AuthorityResult.SUCCESS;
    }

    @Override
    public boolean sootInsert(String dexFilePath, String taskDirPath) {
        long length = new File(dexFilePath).length();
        log.info("[sootInsert] length={}", length);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("init");
        initSoot(dexFilePath, taskDirPath);
        PackManager.v().getPack("jtp").add(
                new Transform("jtp.MyTransform", new MyTransform()));
        stopWatch.stop();
        stopWatch.start("handle");
        PackManager.v().runPacks();
        stopWatch.stop();
        stopWatch.start("write");
        PackManager.v().writeOutput();
        stopWatch.stop();
        // log.info(stopWatch.prettyPrint());
        return true;
    }

    private void initSoot(String dexFilePath, String taskDirPath) {
        G.reset();
        //Options.v().set_debug(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_soot_classpath(sootClasspath);
        Options.v().set_android_jars(sootAndroidJarPath);
        Scene.v().addBasicClass(SOOT_INSERT_CLASS, SootClass.SIGNATURES);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Arrays.asList(dexFilePath));
        Options.v().set_force_overwrite(true);
        if (Strings.isNotBlank(taskDirPath)) {
            Options.v().set_output_dir(taskDirPath);
        }
        Scene.v().loadNecessaryClasses();
    }


    private AuthorityResult check(List<File> javaFileList) {
        try {
            for (File javaFile : javaFileList) {
                List<String> imports = JavaAnalysisUtils.getImports(javaFile);

                Optional<Optional<String>> result = imports
                        .stream()
                        .map(importData -> GlobalData.BLACK_IMPORT_LIST
                                .stream()
                                .filter(importData::startsWith)
                                .findFirst())
                        .filter(Optional::isPresent).findFirst();
                if (result.isPresent() && result.get().isPresent()) {
                    log.info("[check] data={} illegal", result);
                    return AuthorityResult.IMPORT_ILLEGAL_USE;
                }
            }
            return AuthorityResult.SUCCESS;
        } catch (Exception e) {
            // Weak check, default success
            log.error("[check] unknown error", e);
            return AuthorityResult.SUCCESS;
        }
    }

    static class MyTransform extends BodyTransformer {

        @Override
        protected void internalTransform(Body body, String arg1,
                                         Map<String, String> arg2) {
            Iterator<Unit> unitsIterator = body.getUnits().snapshotIterator();
            while (unitsIterator.hasNext()) {
                Stmt stmt = (Stmt) unitsIterator.next();
                if (stmt.containsInvokeExpr()) {
                    String declaringClass = stmt.getInvokeExpr().getMethod().getDeclaringClass().getName();
//                    if (declaringClass.startsWith("android")) {
//                        //log.error("[internalTransform] static check finds android import");
//                    }
                    String methodName = stmt.getInvokeExpr().getMethod().getName();
                    List<Value> valueList = stmt.getInvokeExpr().getArgs();
                    if (SOOT_BLACK_METHOD.equals(methodName) && SOOT_BLACK_CLASS.equals(declaringClass)) {
                        List<Unit> checkUnits = makeCheckUnits(body, valueList.get(0).toString());
                        body.getUnits().insertBefore(checkUnits, stmt);
                        // System.out.println("!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                }
            }
        }

        private List<Unit> makeCheckUnits(Body body, String className) {
            List<Unit> unitsList = new ArrayList<Unit>();
            SootClass logClass = Scene.v().getSootClass(SOOT_INSERT_CLASS);
            SootMethod sootMethod = logClass.getMethod(SOOT_INSERT_METHOD_DESC);
            StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(sootMethod.makeRef(), StringConstant.v(className));
            InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(staticInvokeExpr);
            unitsList.add(invokeStmt);
            return unitsList;
        }
    }
}
