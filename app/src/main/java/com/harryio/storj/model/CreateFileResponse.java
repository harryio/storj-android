package com.harryio.storj.model;

public class CreateFileResponse {
    String id;
    String bucket;
    String mimetype;
    String filename;
    long size;

    public String getId() {
        return id;
    }

    public String getMimetype() {
        return mimetype;
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getBucket() {
        return bucket;
    }
}
