package com.jifalops.toolbox.android.file;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 */
public class Files {
    static final String TAG = Files.class.getSimpleName();
    private Files() {}

    public static void copy(File src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;

        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(dst);
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.e(TAG, "file copy error: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                Log.e(TAG, "file copy error: " + e.getMessage());
                throw e;
            }
        }



    }
}
