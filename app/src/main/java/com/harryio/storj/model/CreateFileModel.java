package com.harryio.storj.model;

public class CreateFileModel {
    String frame;
    String mimetype;
    String filename;
    long __nonce;

    public CreateFileModel(String frame, String mimetype, String filename) {
        this.frame = frame;
        this.mimetype = mimetype;
        this.filename = filename;
        this.__nonce = System.currentTimeMillis();
    }
}
