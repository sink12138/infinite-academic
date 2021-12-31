package com.buaa.academic.resource.util;

import java.io.File;
import java.io.IOException;

public class FolderUtils {

    public static void removeFolder(File folder) throws IOException {
        if (folder == null)
            throw new IllegalArgumentException("folder cannot be null");
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null)
                throw new IOException("unexpected null old file list");
            for (File child : files) {
                removeFolder(child);
            }
        }
        if (!folder.delete()) {
            throw new IOException("cannot delete old file");
        }
    }

}
