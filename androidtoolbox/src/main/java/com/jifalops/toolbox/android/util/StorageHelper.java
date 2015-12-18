package com.jifalops.toolbox.android.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 *
 */
public class StorageHelper {
    private static final String TAG = StorageHelper.class.getSimpleName();

    private static StorageHelper instance;
    public static StorageHelper getInstance(Context ctx) {
        if (instance == null) instance = new StorageHelper(ctx);
        return instance;
    }

    private final File externalRoot;
    private final File internalRoot;
    private final File sdcard;

    private StorageHelper(Context ctx) {
        internalRoot = ctx.getFilesDir();
        externalRoot = ctx.getExternalFilesDir(null);
        sdcard = Environment.getExternalStorageDirectory();
    }


    private File getFile(File root, String relativePath) {
        return getFile(root, relativePath, true);
    }
    private File getFile(File root, String relativePath, boolean mkdirs) {
        if (relativePath == null) return root;
        File f = new File(externalRoot, relativePath);
        if (mkdirs && !f.exists()) {
            if (!f.mkdirs()) {
                Log.e(TAG, "Failed to make directories for " + f.getAbsolutePath());
            }
        }
        return f;
    }

    public File getAppExternalFile(String file) {
        return getAppExternalFile(file, true);
    }
    public File getAppExternalFile(String file, boolean mkdirs) {
        return getFile(externalRoot, file, mkdirs);
    }

    public File getAppInternalFile(String file) {
        return getAppInternalFile(file, true);
    }
    public File getAppInternalFile(String file, boolean mkdirs) {
        return getFile(internalRoot, file, mkdirs);
    }

    public File getSdcardFile(String file) {
        return getSdcardFile(file, true);
    }
    public File getSdcardFile(String file, boolean mkdirs) {
        return getFile(sdcard, file, mkdirs);
    }
}
