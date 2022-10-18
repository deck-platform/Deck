package com.osfans.trime.fl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static byte[] getFileContent(File file) throws Exception {
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            throw new Exception("File size so large");
        }

        byte[] buffer = new byte[(int) fileSize];
        FileInputStream fi = new FileInputStream(file);
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        fi.close();
        // Make sure all content has been read
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return buffer;
    }

    public static void writeByteArrayToFile(String path, byte[] byteArray) throws Exception {
        FileOutputStream os = new FileOutputStream(path);
        os.write(byteArray);
        os.close();
    }
}
