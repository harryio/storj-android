package com.harryio.storj.model;

import java.util.List;

public class CreateBucketModel {
    int storage;
    int transfer;
    List<String> pubkeys;
    String name;
    long __nonce;

    public CreateBucketModel(int storage, int transfer, List<String> pubkeys, String name) {
        this.storage = storage;
        this.transfer = transfer;
        this.pubkeys = pubkeys;
        this.name = name;
        this.__nonce = System.currentTimeMillis() / 1000L;
    }
}
