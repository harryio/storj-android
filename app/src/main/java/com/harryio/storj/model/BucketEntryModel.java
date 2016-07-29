package com.harryio.storj.model;

import java.util.UUID;

public class BucketEntryModel {
    String frame;
    String mimetype;
    String filename;
    String nonce;

    public BucketEntryModel(String frame, String mimetype, String filename) {
        this.frame = frame;
        this.mimetype = mimetype;
        this.filename = filename;
        this.nonce = UUID.randomUUID().toString();
    }
}
