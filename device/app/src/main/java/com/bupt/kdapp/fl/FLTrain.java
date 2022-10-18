package com.bupt.kdapp.fl;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class FLTrain {
    public void run() throws Exception {
        String flPath = "/data/data/com.bupt.deck/files";
        File dexFile = new File(flPath, "classes.dex");

        File libDir = new File("/data/data/com.bupt.deck/FLLibs");

        DexClassLoader classLoader = new DexClassLoader(dexFile.getAbsolutePath(), null, libDir.getAbsolutePath(), getClass().getClassLoader());
        Class<?> cls = classLoader.loadClass("fltrain.FLTrain");
        Object instance = cls.newInstance();
        Method run = cls.getDeclaredMethod("run");
        run.invoke(instance);
    }
}
