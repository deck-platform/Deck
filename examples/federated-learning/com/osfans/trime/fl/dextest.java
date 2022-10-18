package com.osfans.trime.fl;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

import deck.wrapper.ContextWrapper;

public class dextest {

     private static String TRAIN_MODEL_FILE_PATH = "/data/data/com.osfans.trime/files/2_mnist.snapshot.mnn";
     private static String TRAIN_DATA_FILE_PATH = "/data/data/com.osfans.trime/files/mnist_data";

     public String run(ContextWrapper contextWrapper) throws Exception {
         /** 
          * After training, new model will overwrite origin model at TRAIN_MODEL_FILE_PATH. 
          * So we need to read new model file into byteArray, serialize to string and return.
          */
         System.out.println(TRAIN_MODEL_FILE_PATH + "; " + TRAIN_DATA_FILE_PATH);
         final String result = MNNDataNative.nativeCreateDatasetFromFile(TRAIN_MODEL_FILE_PATH, TRAIN_DATA_FILE_PATH);
         System.out.println(result);
         File newModel = new File(TRAIN_MODEL_FILE_PATH);
         if (!newModel.exists()) {
            // If newModel does not exist, throws an Exception on android
            throw new Exception("Trained model not found");
         } else {
            byte[] modelData = FileUtil.getFileContent(newModel);
            /** 
             * Return format: Map<String, String>
             *  "model": model 
             *  "result": MNN train result (loss, accuracy) 
             */
            Map<String, String> retMap = new HashMap<>();
            retMap.put("model", Base64.getEncoder().encodeToString(modelData));
            retMap.put("result", result);
            JSONObject jsonObject = new JSONObject(retMap);
            return jsonObject.toString(); 
         }
     }
}