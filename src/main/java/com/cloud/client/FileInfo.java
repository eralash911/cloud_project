package com.cloud.client;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    final private String fileName;
    final private long fileSize;
    final private boolean isDirectory;

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isDirectory()   {
        return isDirectory;
    }

    public FileInfo(Path path){
        fileName = path.getFileName().toString();
        isDirectory = Files.isDirectory(path);
        if (!isDirectory){
            fileSize = path.toFile().length();
        }else {
            fileSize = 0;
        }

    }

}
