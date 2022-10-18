package com.bupt.deck.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class FileHelper {
    static String TAG = "FileHelper-Deck";

    public static void writeFile(String parentPath, byte[] byteArray, String fileName) {
        File file = new File(parentPath, fileName);
        if (!file.exists()) {
            try {
                boolean createSuccess = file.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "writeFile Error: " + e.toString());
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(byteArray);
            out.close();
        } catch (Exception e) {
            Log.e(TAG, "writeFile Error: " + e.toString());
        }
    }

    public static void writeFile(String parentPath, String base64String, String fileName) {
        File file = new File(parentPath, fileName);
        if (!file.exists()) {
            try {
                boolean createSuccess = file.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "writeFile: Error: " + e.toString());
            }
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] byteArray = stringToByteArray(base64String);
            out.write(byteArray);
            out.close();
            Log.i(TAG, "writeFile: write byteArray to file "
                    + file.getAbsolutePath() + " successfully");
        } catch (Exception e) {
            Log.e(TAG, "writeFile: Error: " + e.toString());
        }
    }

    public static byte[] stringToByteArray(String s) {
        byte[] b = s.getBytes(StandardCharsets.US_ASCII);
        return android.util.Base64.decode(b, android.util.Base64.DEFAULT);
    }

}
