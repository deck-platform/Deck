package com.bupt.kdapp;

import android.app.Application;
import android.util.Log;

import com.bupt.kdapp.sqlite.SysInfoDatabaseHelper;
import com.bupt.deck.utils.PreferenceHelper;

import java.io.File;

import static com.bupt.deck.utils.AssetsHelper.copyAssetFolder;

public class GlobalClass extends Application {
    String TAG = "GlobalClass-Deck";

    @Override
    public void onCreate() {
        super.onCreate();

        // Check if this app is first installed, copy images in assets to app private dir
        if (!PreferenceHelper.getBoolean(getApplicationContext(), "installed")) {
            PreferenceHelper.put(getApplicationContext(), "installed", true);

            // Delete files in app external dir when first installation
            File file = new File(getExternalFilesDir(null).getAbsolutePath());
            for (String fileName : file.list()) {
                Log.i(TAG, "onCreate: remove file " + fileName);
                new File(file.getAbsolutePath(), "/" + fileName).delete();
            }

            // Sync sysinfo.db in assets/database/ to app private directory
            SysInfoDatabaseHelper helper = new SysInfoDatabaseHelper(this);
            helper.getReadableDatabase();

            // Copy images in assets/imgs/ to com.bupt.kubrdroid/files
            copyAssetFolder(getAssets(), "imgs",
                    getApplicationContext().getFilesDir().getAbsolutePath());


        }
    }

}
