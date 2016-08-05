package com.harryio.storj.model;

public class StorjFile {
    String hash;
    String bucket;
    String mimetype;
    String filename;
    long size;
    String id;

    public String getHash() {
        return hash;
    }

    public String getBucket() {
        return bucket;
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

    public String getId() {
        return id;
    }
}
