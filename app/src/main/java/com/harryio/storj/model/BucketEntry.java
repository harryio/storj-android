package com.harryio.storj.model;

public class BucketEntry {
    String id;
    String bucket;
    String mimetype;
    String filename;
    long size;

    @Override
    public String toString() {
        return "StoreFile{" +
                "id='" + id + '\'' +
                ", bucket='" + bucket + '\'' +
                ", mimetype='" + mimetype + '\'' +
                ", filename='" + filename + '\'' +
                ", size=" + size +
                '}';
    }
}
