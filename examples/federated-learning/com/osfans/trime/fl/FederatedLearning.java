package com.osfans.trime.fl;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import deck.DeviceFuture;
import deck.Deck;
import deck.Task;
import deck.FL;

public class FederatedLearning {
    static final String taskDescription = "FLTraining";

    public static void writeByteArrayToFile(String path, byte[] byteArray) throws Exception {
        FileOutputStream os = new FileOutputStream(path);
        os.write(byteArray);
        os.close();
    }

    public static void main(String[] args) {
        Deck.init("trime.kddev.host:9999");
        String exampleDir = Paths.get("").toAbsolutePath().toString() + File.separatorChar;
        String libPath = exampleDir + ".." + File.separatorChar;
        try {
            /**
             * Deck.constructTask param: (compile_target_java_file, lib_search_path, output_path, task_description)
             */
            Task task = FL.constructTask(
                new File("com/osfans/trime/fl/dextest.java"), 
                new File(libPath), 
                exampleDir, 
                taskDescription, 18, 15);
            System.out.println("===taskID: " + task.getTaskid());
            List<DeviceFuture> futures = task.run();
            String aggModel = FL.getModel(futures, 10000);
            /**
             *  Developer got an aggregated model from gateway in String, then deserialize it to byteArray
             *  and save it to disk.
             */
            byte[] byteArray = Base64.getDecoder().decode(aggModel);
            writeByteArrayToFile("trainedModel.mnn", byteArray);
            List<String> traces = FL.getTrainTraces(futures);
            System.out.println(traces);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}