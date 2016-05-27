package com.harryio.storj.util;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileUtils {
    private FileUtils() {}

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     *         null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    /**
     * @return The MIME type for the given file.
     */
    public static String getMimeType(File file) {

        String extension = getExtension(file.getName());

        if (extension.length() > 0)
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

        return "application/octet-stream";
    }

    public static File getFileFromUri(Uri uri) {
        return new File(uri.getPath());
    }

    public static String getFilename(File file) {
        String path = file.getPath();
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    public static byte[] fileToByteArray(File file) {
        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }

    public static byte[] getPaddedByteArray(File file) {
        byte[] array = fileToByteArray(file);

        int length = array.length;
        int capacity = (int) Math.pow(2, Math.ceil(Math.log(length) / Math.log(2)));

        return Arrays.copyOf(array, capacity);
    }
}
