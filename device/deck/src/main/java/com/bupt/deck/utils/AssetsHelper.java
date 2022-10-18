package com.bupt.deck.utils;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetsHelper {
    static String TAG = "AssetsHelper-Deck";

    public static boolean copyAsset(AssetManager assetManager,
                                    String fromAssetPath, String toPath) {
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            Log.i(TAG, "copyAsset: create new file: " + toPath);
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static boolean copyAssetFolder(AssetManager assetManager,
                                    String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            if (files == null) {
                Log.w(TAG, "copyAssetFolder: no files in assets/" + fromAssetPath);
                return false;
            }
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files) {
                // if (file.contains("."))
                res &= copyAsset(assetManager,
                        fromAssetPath + "/" + file,
                        toPath + "/" + file);
                // else
                //     res &= copyAssetFolder(assetManager,
                //             fromAssetPath + "/" + file,
                //             toPath + "/" + file);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
